// Track injected tabs and detected threats
let injectedTabs = new Set();
let detectedThreats = new Map();

// Store the Flockx API key securely
let apiKey = null;

// Cache for analyzed emails
const analyzedEmails = new Map();

// Initialize extension
chrome.runtime.onInstalled.addListener(() => {
    console.log('[DEBUG] Email Phishing Guard installed');
});

// Function to show popup
async function showPopupForTab(tabId) {
    try {
        console.log('[DEBUG] Updating badge for tab:', tabId);
        const threats = detectedThreats.get(tabId) || [];
        console.log('[DEBUG] Current threats:', threats);
        
        if (threats.length > 0) {
            await chrome.action.setBadgeText({ text: threats.length.toString(), tabId });
            await chrome.action.setBadgeBackgroundColor({ color: '#d32f2f', tabId });
        } else {
            await chrome.action.setBadgeText({ text: '', tabId });
        }
    } catch (error) {
        console.error('[DEBUG] Error updating badge:', error);
    }
}

// Function to broadcast threats to all listeners
async function broadcastThreats(tabId, threats) {
    try {
        console.log('[DEBUG] Broadcasting threats for tab', tabId, ':', threats);
        await chrome.runtime.sendMessage({
            type: 'THREATS_UPDATED',
            threats: threats,
            tabId: tabId
        }).catch(error => {
            // This error is expected when popup is closed
            console.log('[DEBUG] No listeners for broadcast (this is normal if popup is closed)');
        });
    } catch (error) {
        console.error('[DEBUG] Error broadcasting threats:', error);
    }
}

// Function to make API calls to Flockx via proxy
async function analyzeWithFlockx(content) {
  try {
    // Add retry logic with exponential backoff
    const maxRetries = 3;
    let retryCount = 0;
    let lastError = null;

    while (retryCount < maxRetries) {
      try {
        const response = await fetch('http://localhost:3000/api/proxy/flockx', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${apiKey}`
          },
          body: JSON.stringify({ content })
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(
            errorData?.error || 
            `API request failed with status ${response.status}`
          );
        }

        const data = await response.json();
        return data;
      } catch (error) {
        lastError = error;
        if (error.message.includes('Failed to fetch') || 
            error.message.includes('NetworkError')) {
          // Only retry on network errors
          retryCount++;
          if (retryCount < maxRetries) {
            // Exponential backoff: wait 2^retryCount seconds
            await new Promise(resolve => 
              setTimeout(resolve, Math.pow(2, retryCount) * 1000)
            );
            continue;
          }
        }
        throw error;
      }
    }
    throw lastError;
  } catch (error) {
    console.error('[DEBUG] API error:', error);
    // Provide more specific error messages
    if (error.message.includes('Failed to fetch')) {
      throw new Error('Unable to connect to server. Please check if the server is running.');
    } else if (error.message.includes('NetworkError')) {
      throw new Error('Network error while connecting to server. Please try again.');
    } else if (error.message.includes('401')) {
      throw new Error('Authentication failed. Please check your credentials.');
    } else if (error.message.includes('429')) {
      throw new Error('Too many requests. Please try again later.');
    }
    throw error;
  }
}

// Function to notify guardians
async function notifyGuardians(threats, emailContent) {
  try {
    const response = await fetch('http://localhost:3000/api/notify-guardians', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        threats,
        emailContent,
        token: localStorage.getItem('phishyguard_token')
      })
    });

    if (!response.ok) {
      throw new Error('Failed to notify guardians');
    }
  } catch (error) {
    console.error('Guardian notification error:', error);
  }
}

// Message handling
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === 'ANALYZE_EMAIL') {
    analyzeWithFlockx(message.content)
      .then(async result => {
        // Cache the results
        if (sender.tab) {
          analyzedEmails.set(sender.tab.id, result);
        }

        // If threats are detected, notify guardians
        if (result.threats && result.threats.length > 0) {
          await notifyGuardians(result.threats, message.content);
        }

        sendResponse(result);
      })
      .catch(error => {
        sendResponse({ error: error.message });
      });
    return true; // Keep the message channel open
  }

  if (message.type === 'GET_ANALYSIS') {
    const analysis = sender.tab ? analyzedEmails.get(sender.tab.id) : null;
    sendResponse(analysis || { message: 'No analysis available' });
    return true;
  }

  if (message.type === 'SET_API_KEY') {
    apiKey = message.apiKey;
    sendResponse({ success: true });
    return true;
  }

  // Handle other message types
  if (message.type === 'THREAT_DETECTED') {
    // Store response for the tab
    const tabId = sender.tab.id;
    detectedThreats.set(tabId, message.response);
    showPopupForTab(tabId);
    broadcastThreats(tabId, message.response);
    return true;
  }

  // Handle get threats request from popup
  if (message.type === 'GET_THREATS') {
    chrome.tabs.query({ active: true, currentWindow: true }, function(tabs) {
        if (tabs[0]) {
            const tabId = tabs[0].id;
            const response = detectedThreats.get(tabId);
            console.log('[DEBUG] Getting analysis for tab', tabId, ':', response);
            sendResponse({ response, tabId });
        } else {
            console.log('[DEBUG] No active tab found for GET_THREATS');
            sendResponse({ response: null, tabId: null });
        }
    });
    return true;
  }

  // Handle scan request from popup
  if (message.type === 'SCAN_EMAIL') {
    chrome.tabs.query({ active: true, currentWindow: true }, async function(tabs) {
        if (tabs[0]) {
            const tabId = tabs[0].id;
            console.log('[DEBUG] Sending scan request to tab', tabId);
            try {
                const response = await chrome.tabs.sendMessage(tabId, { type: 'ANALYZE_EMAIL' });
                console.log('[DEBUG] Scan response:', response);
                sendResponse(response);
            } catch (error) {
                console.error('[DEBUG] Error during scan:', error);
                sendResponse({ error: 'Failed to analyze email' });
            }
        } else {
            sendResponse({ error: 'No active tab found' });
        }
    });
    return true;
  }
});

// Function to inject content script
async function injectContentScript(tabId) {
    try {
        if (injectedTabs.has(tabId)) {
            console.log('Content script already injected in tab:', tabId);
            return;
        }

        await chrome.scripting.executeScript({
            target: { tabId: tabId },
            files: ['content.js']
        });

        injectedTabs.add(tabId);
        console.log('Content script injected successfully in tab:', tabId);
    } catch (err) {
        console.error('Failed to inject content script:', err);
    }
}

// Clean up cache when tabs are closed
chrome.tabs.onRemoved.addListener((tabId) => {
    injectedTabs.delete(tabId);
    detectedThreats.delete(tabId);
    analyzedEmails.delete(tabId);
});

// Listen for tab updates
chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
    if (changeInfo.status === 'complete' && tab.url) {
        try {
            const url = new URL(tab.url);
            if (url.hostname.includes('mail.google.com') || url.hostname.includes('outlook')) {
                injectContentScript(tabId);
            }
        } catch (error) {
            console.error('Error handling tab update:', error);
        }
    }
});

// Keep service worker alive
chrome.runtime.onConnect.addListener(port => {
    console.log('Port connected:', port.name);
}); 