// Track injected tabs and detected threats
let injectedTabs = new Set();
let detectedThreats = new Map();

// Initialize extension
chrome.runtime.onInstalled.addListener(() => {
    console.log('Email Phishing Guard installed');
    chrome.storage.local.clear(); // Clear any old data
});

// Function to show popup
async function showPopupForTab(tabId) {
    try {
        // Get the current popup state
        const popup = await chrome.action.getPopup({ tabId });
        if (!popup) {
            // Only show if popup isn't already open
            await chrome.action.openPopup();
        }
    } catch (error) {
        console.error('Error showing popup:', error);
    }
}

// Listen for threat detection messages from content script
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'THREAT_DETECTED' && sender.tab) {
        const tabId = sender.tab.id;
        const threats = message.threats;

        // Store threats for this tab
        detectedThreats.set(tabId, threats);

        // Show popup
        showPopupForTab(tabId);

        // Optional: Show a notification
        chrome.notifications.create({
            type: 'basic',
            iconUrl: 'icons/icon15.png',
            title: 'Phishing Threat Detected!',
            message: `Found ${threats.length} suspicious pattern${threats.length > 1 ? 's' : ''} in the email.`,
            priority: 2
        });

        sendResponse({ success: true });
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

// Handle tab removal
chrome.tabs.onRemoved.addListener((tabId) => {
    injectedTabs.delete(tabId);
    detectedThreats.delete(tabId);
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