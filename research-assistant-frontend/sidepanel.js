document.addEventListener('DOMContentLoaded', () => {
    chrome.storage.local.get(['researchNotes'], function(result) {
        if (result.researchNotes) {
            document.getElementById('notes').value = result.researchNotes;
        }
    });

    document.getElementById('operationBtn').addEventListener('click', operationText);
    document.getElementById('saveNotesBtn').addEventListener('click', saveNotes);
});

async function operationText() {
    const btn = document.getElementById('operationBtn');
    const results = document.getElementById('results');
    const select = document.getElementById('operation');
    const op = select.value;

    // show loading UI
    btn.classList.add('loading');
    results.classList.add('loading');
    results.innerHTML = '';

    try {
        const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
        const [{ result }] = await chrome.scripting.executeScript({
            target: { tabId: tab.id },
            function: () => window.getSelection().toString()
        });

        if (!result) {
            showResult('Please select some text first');
            return;
        }

        const response = await fetch('http://localhost:8080/api/research/process', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: result, operation: op })
        });

        if (!response.ok) {
            throw new Error(`API Error: ${response.status}`);
        }

        const text = await response.text();
        showResult(text.replace(/\n/g, '<br>'));
    } catch (error) {
        showResult('Error: ' + error.message);
    } finally {
        btn.classList.remove('loading');
        results.classList.remove('loading');
    }
}

async function saveNotes() {
    const notes = document.getElementById('notes').value;
    chrome.storage.local.set({ 'researchNotes': notes }, function() {
        alert('Notes saved successfully');
    });
}

function showResult(content) {
    document.getElementById('results').innerHTML =
        `<div class="result-item"><div class="result-content">${content}</div></div>`;
}
