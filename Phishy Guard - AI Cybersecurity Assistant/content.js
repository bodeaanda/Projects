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
function getGmailContent() {
    try {
        const selectors = [
            '.a3s.aiL',
            '.ii.gt',
            '[role="main"] .a3s.aiL',
            '.gs .ii.gt .a3s.aiL'
        ];

        for (const selector of selectors) {
            const emailBody = safeQuerySelector(selector);
            if (emailBody && emailBody.innerText.trim()) {
                // Get subject
                const subject = safeQuerySelector('.hP')?.innerText || 
                              safeQuerySelector('h2.ha')?.innerText || 
                              'No subject';

                // Get sender
                const sender = safeQuerySelector('.gD[email]')?.getAttribute('email') ||
                             safeQuerySelector('.go[email]')?.getAttribute('email') ||
                             safeQuerySelector('.g2[email]')?.getAttribute('email') ||
                             'Unknown sender';

                return {
                    content: emailBody.innerText,
                    subject,
                    sender
                };
            }
        }
        return null;
    } catch (error) {
        console.error('Error extracting Gmail content:', error);
        return null;
    }
}

// Function to extract email content from Outlook
function getOutlookContent() {
    try {
        const selectors = [
            '[role="main"] .allowTextSelection',
            '.ReadingPaneContents',
            '[role="main"] [role="document"]'
        ];

        for (const selector of selectors) {
            const emailBody = safeQuerySelector(selector);
            if (emailBody && emailBody.innerText.trim()) {
                return {
                    content: emailBody.innerText,
                    subject: safeQuerySelector('[role="heading"]')?.innerText || 'No subject',
                    sender: safeQuerySelector('.RPVEf')?.innerText || 'Unknown sender'
                };
            }
        }
        return null;
    } catch (error) {
        console.error('Error extracting Outlook content:', error);
        return null;
    }
}

// Function to analyze email for phishing attempts
function analyzeEmail(emailData) {
    const threats = [];
    
    // Common phishing patterns
    const patterns = [
        {
            pattern: /urgent|immediate action required|account.*suspended/i,
            threat: 'Urgency manipulation'
        },
        {
            pattern: /verify.*account|confirm.*identity|security.*check/i,
            threat: 'Account verification scam'
        },
        {
            pattern: /password.*expired|security.*breach/i,
            threat: 'Password-related scam'
        },
        {
            pattern: /suspicious.*activity|unusual.*login/i,
            threat: 'Fake security alert'
        },
        {
            pattern: /click.*here.*to.*verify|click.*here.*to.*confirm/i,
            threat: 'Suspicious link pattern'
        },
        {
            pattern: /bank.*account|credit.*card|payment.*details/i,
            threat: 'Financial information request'
        },
        {
            pattern: /won|winner|lottery|prize|inheritance/i,
            threat: 'Prize or lottery scam'
        },
        {
            pattern: /bitcoin|cryptocurrency|investment.*opportunity/i,
            threat: 'Cryptocurrency scam'
        }
    ];

    // Check content against patterns
    for (const {pattern, threat} of patterns) {
        if (pattern.test(emailData.content.toLowerCase())) {
            threats.push(threat);
        }
    }

    // Check for suspicious sender patterns
    if (emailData.sender.includes('noreply') || 
        emailData.sender.includes('account') || 
        emailData.sender.includes('security') || 
        emailData.sender.includes('admin')) {
        threats.push('Suspicious sender address');
    }

    // Check for mismatched sender domains with common services
    const servicePatterns = {
        'google': {
            domains: ['google.com', 'gmail.com', 'googlemail.com'],
            patterns: [
                /google.*account.*verify/i,
                /google.*security.*alert/i,
                /sign.?in.*attempt/i,
                /google.*password.*reset/i
            ]
        },
        'microsoft': {
            domains: ['microsoft.com', 'outlook.com', 'live.com', 'hotmail.com'],
            patterns: [
                /microsoft.*account.*verify/i,
                /microsoft.*security.*alert/i,
                /outlook.*password.*reset/i
            ]
        },
        'apple': {
            domains: ['apple.com', 'icloud.com'],
            patterns: [
                /apple.*id.*verify/i,
                /icloud.*account.*locked/i,
                /apple.*purchase.*confirm/i
            ]
        },
        'paypal': {
            domains: ['paypal.com', 'paypal.co'],
            patterns: [
                /paypal.*account.*limit/i,
                /unusual.*activity.*paypal/i,
                /paypal.*payment.*confirm/i
            ]
        },
        'amazon': {
            domains: ['amazon.com', 'amazon.co'],
            patterns: [
                /amazon.*account.*verify/i,
                /amazon.*order.*cancel/i,
                /amazon.*payment.*fail/i
            ]
        }
    };

    // More sophisticated service impersonation check
    for (const [service, config] of Object.entries(servicePatterns)) {
        // Only check if content has concerning patterns
        const hasPhishingPattern = config.patterns.some(pattern => 
            pattern.test(emailData.content.toLowerCase())
        );
        
        if (hasPhishingPattern) {
            // Check if sender is from legitimate domain
            const isLegitSender = config.domains.some(domain =>
                emailData.sender.toLowerCase().endsWith(domain)
            );
            
            if (!isLegitSender) {
                threats.push(`Potential ${service} impersonation`);
            }
        }
    }

    return threats;
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

// Function to automatically check email content
async function checkEmailContent() {
    try {
        const hostname = window.location.hostname.toLowerCase();
        let emailData = null;

        if (hostname.includes('mail.google.com')) {
            emailData = getGmailContent();
        } else if (hostname.includes('outlook')) {
            emailData = getOutlookContent();
        }

        if (emailData && emailData.content) {
            const contentHash = getContentHash(emailData.content);
            
            // Only analyze if content has changed
            if (contentHash !== lastAnalyzedContent) {
                lastAnalyzedContent = contentHash;
                console.log('Analyzing new email content...');

                const threats = analyzeEmail(emailData);
                
                if (threats.length > 0) {
                    console.log('Threats detected:', threats);
                    // Notify background script
                    try {
                        await chrome.runtime.sendMessage({
                            type: 'THREAT_DETECTED',
                            threats: threats
                        });
                    } catch (error) {
                        console.error('Error sending threat notification:', error);
                    }
                }
            }
        }
    } catch (error) {
        console.error('Error in automatic email check:', error);
    }
}

// Initialize mutation observer to detect email changes
function initializeObserver() {
    const observer = new MutationObserver(() => {
        if (observerTimeout) {
            clearTimeout(observerTimeout);
        }
        observerTimeout = setTimeout(() => {
            checkEmailContent();
        }, 1000); // Debounce for 1 second
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true
    });

    // Initial check
    checkEmailContent();
}

// Handle message requests
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.type === 'ANALYZE_EMAIL') {
        try {
            console.log('Manual analysis requested...');
            const hostname = window.location.hostname.toLowerCase();
            let emailData = null;

            if (hostname.includes('mail.google.com')) {
                emailData = getGmailContent();
            } else if (hostname.includes('outlook')) {
                emailData = getOutlookContent();
            }

            if (!emailData || !emailData.content) {
                console.log('No email content found');
                sendResponse({
                    error: 'No email content found. Please make sure an email is open.'
                });
                return true;
            }

            console.log('Analyzing email content...');
            const threats = analyzeEmail(emailData);
            console.log('Analysis complete. Threats found:', threats.length);
            sendResponse({ threats });

        } catch (error) {
            console.error('Error analyzing email:', error);
            sendResponse({
                error: 'Error analyzing email content: ' + error.message
            });
        }
    }
    return true;
});

// Initialize observer when script loads
initializeObserver(); 