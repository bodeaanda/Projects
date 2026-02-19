document.addEventListener('DOMContentLoaded', () => {
    const scanButton = document.getElementById('scanButton');
    const statusTitle = document.getElementById('statusTitle');
    const statusMessage = document.getElementById('statusMessage');
    const statusBox = document.getElementById('statusBox');
    const statusText = document.getElementById('statusText');
    const details = document.getElementById('details');
    const icon = statusBox.querySelector('.icon');

    let currentTabId = null;

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
        console.log('[DEBUG] Updating status:', { type, message, detailsText });
        statusBox.className = `status ${type}`;
        statusText.textContent = message;
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

    // Function to check current threats
    async function checkCurrentThreats() {
        try {
            console.log('[DEBUG] Checking current threats');
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            if (!tab) {
                console.log('[DEBUG] No active tab found');
                updateStatus('warning', 'No active tab found');
                return;
            }

            currentTabId = tab.id;
            console.log('[DEBUG] Current tab ID:', currentTabId);

            // Get current threats from background script
            chrome.runtime.sendMessage({ type: 'GET_THREATS' }, response => {
                console.log('[DEBUG] Got threats response:', response);
                if (response && response.threats) {
                    if (response.threats.length > 0) {
                        const threatList = response.threats
                            .map(threat => `∙ ${threat}`)
                            .join('\n');
                        
                        updateStatus('danger', 'Potential threats detected!', 
                            `AI analysis found the following concerns:\n${threatList}`);
                    } else {
                        updateStatus('safe', 'No threats detected', 
                            'AI analysis indicates this email appears to be safe.');
                    }
                }
            });
        } catch (error) {
            console.error('[DEBUG] Error checking threats:', error);
            updateStatus('warning', 'Error checking threats', error.message);
        }
    }

    // Listen for threat updates from background script
    chrome.runtime.onMessage.addListener((message) => {
        console.log('[DEBUG] Received message in popup:', message);
        
        if (message.type === 'THREATS_UPDATED' && message.tabId === currentTabId) {
            console.log('[DEBUG] Processing threat update for current tab');
            if (message.threats && message.threats.length > 0) {
                const threatList = message.threats
                    .map(threat => `∙ ${threat}`)
                    .join('\n');
                
                updateStatus('danger', 'Potential threats detected!', 
                    `AI analysis found the following concerns:\n${threatList}`);
            } else {
                updateStatus('safe', 'No threats detected', 
                    'AI analysis indicates this email appears to be safe.');
            }
        }
    });

    // Function to update the UI based on scan results
    function updateUI(result) {
        if (!result || result.error) {
            statusTitle.className = 'status-title warning';
            statusTitle.textContent = 'Scan Failed';
            statusMessage.textContent = result?.error || 'Unable to analyze email';
            return;
        }

        if (result.threats && result.threats.length > 0) {
            statusTitle.className = 'status-title danger';
            statusTitle.textContent = 'Threats Detected';
            statusMessage.textContent = result.threats.join('\n');
        } else {
            statusTitle.className = 'status-title safe';
            statusTitle.textContent = 'No Threats Detected';
            statusMessage.textContent = result.message || 'This email appears to be safe.';
        }
    }

    // Check for existing analysis when popup opens
    chrome.runtime.sendMessage({ type: 'GET_ANALYSIS' }, (result) => {
        if (result && result.message !== 'No analysis available') {
            updateUI(result);
        }
    });

    // Handle scan button click
    scanButton.addEventListener('click', async () => {
        scanButton.disabled = true;
        statusTitle.className = 'status-title';
        statusTitle.textContent = 'Scanning...';
        statusMessage.textContent = 'Analyzing the current email...';

        try {
            // Get the current tab
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            
            if (!tab) {
                throw new Error('No active tab found');
            }

            // Inject content script if not already injected
            await chrome.scripting.executeScript({
                target: { tabId: tab.id },
                files: ['content.js']
            });

            // Request email content from content script
            const response = await chrome.tabs.sendMessage(tab.id, { type: 'GET_EMAIL_CONTENT' });
            
            if (!response || !response.content) {
                throw new Error('No email content found');
            }

            // Send content to background script for analysis
            chrome.runtime.sendMessage(
                { type: 'ANALYZE_EMAIL', content: response.content },
                (result) => {
                    updateUI(result);
                    scanButton.disabled = false;
                }
            );
        } catch (error) {
            updateUI({ error: error.message });
            scanButton.disabled = false;
        }
    });

    // Check threats when popup opens
    checkCurrentThreats();
}); 