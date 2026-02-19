// Guard against multiple injections
if (window.hasOwnProperty('phishingGuardLoaded')) {
    console.log('PhishingGuard already loaded');
} else {
    window.phishingGuardLoaded = true;
}

// Global state
let lastAnalyzedContent = null;
let observerTimeout = null;

// Function to safely query DOM elements
function safeQuerySelector(selector, context = document) {
    try {
        return context.querySelector(selector);
    } catch (error) {
        console.error(`Error querying selector ${selector}:`, error);
        return null;
    }
}

// Function to extract email content from Gmail
function extractGmailContent() {
    const emailBody = document.querySelector('.a3s.aiL');
    const subject = document.querySelector('h2.hP');
    const sender = document.querySelector('.gD');

    if (!emailBody) {
        return null;
    }

    return {
        subject: subject?.textContent || '',
        sender: sender?.getAttribute('email') || '',
        body: emailBody.textContent.trim()
    };
}

// Function to extract email content from Outlook
function extractOutlookContent() {
    const emailBody = document.querySelector('[role="main"] .allowTextSelection');
    const subject = document.querySelector('[role="main"] [role="heading"]');
    const sender = document.querySelector('[role="main"] [aria-label*="From"]');

    if (!emailBody) {
        return null;
    }

    return {
        subject: subject?.textContent || '',
        sender: sender?.textContent.split('<')[1]?.replace('>', '') || '',
        body: emailBody.textContent.trim()
    };
}

// Function to get email content based on the current site
function getEmailContent() {
    const hostname = window.location.hostname.toLowerCase();
    let content = null;

    if (hostname.includes('mail.google.com')) {
        content = extractGmailContent();
    } else if (hostname.includes('outlook')) {
        content = extractOutlookContent();
    }

    if (!content) {
        return { error: 'No email content found or unsupported email client' };
    }

    return {
        content: {
            ...content,
            url: window.location.href,
            timestamp: new Date().toISOString()
        }
    };
}

// Listen for messages from the popup
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'GET_EMAIL_CONTENT') {
        sendResponse(getEmailContent());
    }
    return true;
});

// Phishing detection patterns
const PHISHING_PATTERNS = {
    urgency: [
        /urgent/i,
        /immediate action/i,
        /account.*suspend/i,
        /security.*breach/i,
        /limited time/i
    ],
    threats: [
        /account.*terminat/i,
        /legal action/i,
        /lawsuit/i,
        /police/i,
        /arrest/i
    ],
    requestForInfo: [
        /verify.*account/i,
        /confirm.*password/i,
        /update.*payment/i,
        /send.*money/i,
        /bank.*details/i
    ],
    suspiciousLinks: [
        /bit\.ly/i,
        /tinyurl/i,
        /click.*here/i,
        /login.*here/i,
        /sign.*in.*here/i
    ],
    impersonation: [
        /official.*notice/i,
        /bank.*notification/i,
        /paypal.*service/i,
        /microsoft.*team/i,
        /google.*security/i
    ]
};

// Function to analyze email content
async function analyzeEmailContent() {
    console.log('[DEBUG] Starting email analysis');
    let emailContent = '';

    // Get email content based on email client
    if (window.location.hostname.includes('mail.google.com')) {
        const emailData = getGmailContent();
        if (emailData) {
            emailContent = emailData.content;
            console.log('[DEBUG] Found Gmail content');
        }
    } else if (window.location.hostname.includes('outlook')) {
        const emailData = getOutlookContent();
        if (emailData) {
            emailContent = emailData.content;
            console.log('[DEBUG] Found Outlook content');
        }
    }

    if (!emailContent) {
        console.log('[DEBUG] No email content found');
        return ['Could not access email content'];
    }

    console.log('[DEBUG] Sending content to background script for analysis');

    try {
        // Check if extension context is still valid
        if (!chrome.runtime?.id) {
            console.warn('[DEBUG] chrome.runtime.id is undefined — context likely invalidated');
            return ['Extension context invalidated. Please refresh the page.'];
        }

        // Send content to background script for analysis
        const response = await new Promise((resolve, reject) => {
            chrome.runtime.sendMessage({
                type: 'ANALYZE_CONTENT',
                content: emailContent
            }, (response) => {
                if (chrome.runtime.lastError) {
                    reject(new Error(chrome.runtime.lastError.message));
                } else {
                    resolve(response);
                }
            });
        });

        if (!response) {
            throw new Error('No response from background script');
        }

        if (response.error) {
            throw new Error(response.error);
        }

        const threats = response.threats || [];
        console.log('[DEBUG] Analysis complete. Total threats found:', threats.length);
        return threats;
    } catch (error) {
        console.error('[DEBUG] Error analyzing email:', error);
        if (error.message.includes('Extension context invalidated')) {
            // Handle extension context invalidation gracefully
            console.log('[DEBUG] Extension context invalidated, will retry on next content change');
            return [];
        }
        return ['Error analyzing email content: ' + error.message];
    }
}

// Function to get content hash
function getContentHash(content) {
    let hash = 0;
    for (let i = 0; i < content.length; i++) {
        const char = content.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash;
    }
    return hash.toString();
}

// Function to check email content
async function checkEmailContent() {
    try {
        console.log('Checking email content');
        
        // Get current email content
        let emailData = null;
        if (window.location.hostname.includes('mail.google.com')) {
            emailData = getGmailContent();
        } else if (window.location.hostname.includes('outlook')) {
            emailData = getOutlookContent();
        }

        if (!emailData || !emailData.content) {
            console.log('No email content found');
            return;
        }

        // Check if content has changed
        const contentHash = getContentHash(emailData.content);
        if (contentHash === lastAnalyzedContent) {
            console.log('Content unchanged, skipping analysis');
            return;
        }

        // Update last analyzed content
        lastAnalyzedContent = contentHash;
        
        const threats = await analyzeEmailContent();
        
        if (threats && threats.length > 0) {
            console.log('Sending threat detection message');

            // Check if extension context is still valid before sending message
            if (!chrome.runtime?.id) {
                console.warn('[DEBUG] chrome.runtime.id is undefined — context likely invalidated');
                return;
            }

            // Use a Promise to handle the async message
            await new Promise((resolve) => {
                chrome.runtime.sendMessage({
                    type: 'THREAT_DETECTED',
                    threats: threats
                }, resolve);
            });
            console.log('Threat detection message sent');
        }
    } catch (error) {
        console.error('Error in checkEmailContent:', error);
    }
}

// Initialize observer with longer debounce
const observer = new MutationObserver(() => {
    if (observerTimeout) {
        clearTimeout(observerTimeout);
    }
    observerTimeout = setTimeout(() => {
        checkEmailContent();
    }, 2000); // Increased to 2 seconds
});

observer.observe(document.body, {
    childList: true,
    subtree: true,
    characterData: true
});

// Handle message requests
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    console.log('[DEBUG] Received message:', request.type);
    
    if (request.type === 'ANALYZE_EMAIL') {
        // Create a promise chain to handle the async operations
        Promise.resolve()
            .then(async () => {
                // Check if extension context is still valid
                if (!chrome.runtime?.id) {
                    console.warn('[DEBUG] chrome.runtime.id is undefined — context likely invalidated');
                    sendResponse({ error: 'Extension context invalidated. Please refresh the page.' });
                    return;
                }

                console.log('[DEBUG] Starting manual analysis');
                const threats = await analyzeEmailContent();
                console.log('[DEBUG] Analysis complete. Threats:', threats);
                
                // Send threats to background script first
                console.log('[DEBUG] Sending threats to background script');
                await new Promise((resolve) => {
                    chrome.runtime.sendMessage({
                        type: 'THREAT_DETECTED',
                        threats: threats
                    }, resolve);
                });
                
                // Then respond to the original request
                console.log('[DEBUG] Sending response back');
                sendResponse({ threats });
            })
            .catch(error => {
                console.error('[DEBUG] Error in analysis:', error);
                sendResponse({ error: error.message });
            });
        
        return true; // Keep the message channel open
    }
});

// Function to extract content from Gmail
function getGmailContent() {
  try {
    // Get the main email container
    const emailContainer = document.querySelector('.a3s.aiL');
    if (!emailContainer) {
      console.log('[DEBUG] No email content container found');
      return null;
    }

    // Get email subject
    const subjectElement = document.querySelector('h2.hP');
    const subject = subjectElement ? subjectElement.textContent.trim() : '';

    // Get sender information
    const senderElement = document.querySelector('.gD');
    const sender = senderElement ? {
      name: senderElement.getAttribute('name') || '',
      email: senderElement.getAttribute('email') || ''
    } : null;

    // Get email body
    const content = emailContainer.innerText.trim();

    // Get all links in the email
    const links = Array.from(emailContainer.querySelectorAll('a'))
      .map(a => ({
        text: a.textContent.trim(),
        href: a.href
      }))
      .filter(link => link.href && !link.href.startsWith('mailto:')); // Filter out mailto: links

    // Get email timestamp
    const timestampElement = document.querySelector('.g3');
    const timestamp = timestampElement ? timestampElement.getAttribute('title') : '';

    return {
      subject,
      sender,
      content,
      links,
      timestamp,
      source: 'gmail',
      url: window.location.href
    };
  } catch (error) {
    console.error('[DEBUG] Error extracting Gmail content:', error);
    return null;
  }
}

// Function to extract content from Outlook
function getOutlookContent() {
  try {
    // Get the main email container
    const emailContainer = document.querySelector('[role="main"]');
    if (!emailContainer) {
      console.log('[DEBUG] No Outlook email container found');
      return null;
    }

    // Get email subject
    const subjectElement = document.querySelector('[role="heading"]');
    const subject = subjectElement ? subjectElement.textContent.trim() : '';

    // Get sender information
    const senderElement = document.querySelector('.UxuC');
    const sender = senderElement ? {
      name: senderElement.querySelector('.zF1Zd')?.textContent.trim() || '',
      email: senderElement.querySelector('.OZZZK')?.textContent.trim() || ''
    } : null;

    // Get email body
    const contentElement = document.querySelector('.x_ReadMsgBody, .ReadMsgBody');
    const content = contentElement ? contentElement.innerText.trim() : '';

    // Get all links in the email
    const links = Array.from(emailContainer.querySelectorAll('a'))
      .map(a => ({
        text: a.textContent.trim(),
        href: a.href
      }))
      .filter(link => link.href && !link.href.startsWith('mailto:')); // Filter out mailto: links

    // Get email timestamp
    const timestampElement = document.querySelector('time');
    const timestamp = timestampElement ? timestampElement.getAttribute('datetime') : '';

    return {
      subject,
      sender,
      content,
      links,
      timestamp,
      source: 'outlook',
      url: window.location.href
    };
  } catch (error) {
    console.error('[DEBUG] Error extracting Outlook content:', error);
    return null;
  }
} 