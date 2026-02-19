document.addEventListener('DOMContentLoaded', () => {
    const scanButton = document.getElementById('scanButton');
    const statusBox = document.getElementById('statusBox');
    const statusText = document.getElementById('statusText');
    const details = document.getElementById('details');
    const icon = statusBox.querySelector('.icon');

    // Get theme from storage or system preference
    chrome.storage.sync.get(['theme'], (result) => {
        const savedTheme = result.theme;
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        
        if (savedTheme) {
            document.documentElement.setAttribute('data-theme', savedTheme);
        } else if (prefersDark) {
            document.documentElement.setAttribute('data-theme', 'dark');
            chrome.storage.sync.set({ theme: 'dark' });
        }
    });

    // Theme toggle button handler
    const themeToggle = document.getElementById('themeToggle');
    themeToggle.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        
        document.documentElement.setAttribute('data-theme', newTheme);
        chrome.storage.sync.set({ theme: newTheme });
        
        // Update emoji
        themeToggle.textContent = newTheme === 'dark' ? '🌓' : '☀️';
    });

    function updateStatus(type, message, detailsText = '') {
        statusBox.className = `status ${type}`;
        statusText.textContent = message;
        
        // Format details text with proper bullet points
        if (detailsText && detailsText.includes('•')) {
            detailsText = detailsText.replace(/•/g, '∙'); // Using middle dot instead
        }
        details.textContent = detailsText;
        
        switch(type) {
            case 'safe':
                icon.innerHTML = '&#x2713;'; // Checkmark
                break;
            case 'warning':
                icon.innerHTML = '&#x26A0;'; // Warning sign
                break;
            case 'danger':
                icon.innerHTML = '&#x1F6A8;'; // Police car light
                break;
            default:
                icon.innerHTML = '&#x1F50D;'; // Magnifying glass
        }
    }

    async function scanEmail() {
        scanButton.disabled = true;
        updateStatus('warning', 'Scanning email...', '');

        try {
            // Get the active tab
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            
            if (!tab) {
                throw new Error('No active tab found');
            }

            // Check if we're on a supported email site
            const hostname = new URL(tab.url).hostname.toLowerCase();
            if (!hostname.includes('mail.google.com') && !hostname.includes('outlook')) {
                updateStatus('warning', 'Please open an email in Gmail or Outlook');
                return;
            }

            // Inject content script
            try {
                await chrome.scripting.executeScript({
                    target: { tabId: tab.id },
                    files: ['content.js']
                });
            } catch (error) {
                // Ignore error if script is already injected
                console.log('Content script injection:', error.message);
            }

            // Add a small delay to ensure content script is ready
            await new Promise(resolve => setTimeout(resolve, 100));

            // Send message to content script and wait for response
            const response = await new Promise((resolve, reject) => {
                chrome.tabs.sendMessage(tab.id, { type: 'ANALYZE_EMAIL' }, response => {
                    if (chrome.runtime.lastError) {
                        reject(new Error(chrome.runtime.lastError.message));
                    } else {
                        resolve(response);
                    }
                });
            });

            if (response.error) {
                updateStatus('warning', response.error);
                return;
            }

            if (response.threats && response.threats.length > 0) {
                const threatList = response.threats
                    .slice(0, 3)
                    .map(threat => `∙ ${threat}`)
                    .join('\n');
                
                updateStatus('danger', 'Potential threats detected!', 
                    `Suspicious patterns found:\n${threatList}` +
                    (response.threats.length > 3 ? '\n∙ ...' : ''));
            } else {
                updateStatus('safe', 'No threats detected', 
                    'This email appears to be safe.');
            }

        } catch (error) {
            console.error('Error:', error);
            if (error.message.includes('Cannot establish connection')) {
                updateStatus('warning', 'Connection error', 
                    'Please refresh the page and try again.');
            } else {
                updateStatus('warning', 'Error scanning email', 
                    error.message || 'Please try again.');
            }
        } finally {
            scanButton.disabled = false;
        }
    }

    // Scan automatically when popup opens
    scanEmail();

    // Manual scan button handler
    scanButton.addEventListener('click', scanEmail);
}); 