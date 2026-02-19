import React, { useState } from 'react';

function AccessForm({ API_BASE_URL, handleAccessResponse, writePolicy, missPolicy }) { 
  const [addressInput, setAddressInput] = useState('0'); 
  const [valueInput, setValueInput] = useState('1');
  const [loading, setLoading] = useState(false);
  const handleAccess = async (operation) => {
    setLoading(true);
    
    let address = parseInt(addressInput, 10);
    if (addressInput.toLowerCase().startsWith('0x')) {
        address = parseInt(addressInput, 16);
    }
    
    let url = `${API_BASE_URL}/${operation}?address=${address}`;

    if (operation === 'write') {
        url += `&value=${valueInput}&writePolicy=${writePolicy}&missPolicy=${missPolicy}`;

        try {
            const res = await fetch(url, { method: 'POST' });
            const data = await res.json();
            handleAccessResponse(data);
        } catch(e) { console.error(e); }
    } else {
        try {
            const res = await fetch(url);
            const data = await res.json();
            handleAccessResponse(data);
        } catch(e) { console.error(e); }
    }

    setLoading(false);
  };

  return (
    <div className="access-form control-box">
      <h2 className="panel-title">📲 Memory Access</h2>
      
      <div className="form-group">
        <label>Address (Dec or Hex 0x...):</label>
        <input 
            type="text" 
            value={addressInput} 
            onChange={(e) => setAddressInput(e.target.value)} 
            placeholder="e.g. 1024 or 0x400"
        />
      </div>

      <div className="form-group">
        <label>Value to Write (byte):</label>
        <input 
            type="number" 
            value={valueInput}
            onChange={(e) => setValueInput(e.target.value)}
            max="127" min="-128"
        />
      </div>

      <div className="buttons">
        <button onClick={() => handleAccess('read')} disabled={loading} className="btn-read">READ</button>
        <button onClick={() => handleAccess('write')} disabled={loading} className="btn-write">WRITE</button>
      </div>
    </div>
  );
}

export default AccessForm;