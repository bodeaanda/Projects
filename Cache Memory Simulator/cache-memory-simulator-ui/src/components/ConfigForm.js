import React, { useState, useEffect } from 'react';

const CACHE_SIZES = [64, 128, 256, 512, 1024, 2048, 4096];
const BLOCK_SIZES = [16, 32, 64];
const WAYS = [2, 4, 8];
const MAPPING_TYPES = [
    { label: 'Direct Mapped', value: 'DIRECT' },
    { label: 'N-Way Set Associative', value: 'SET' },
    { label: 'Fully Associative', value: 'FULLY' }
];

function ConfigForm({ API_BASE_URL, fetchData, writePolicy, setWritePolicy, missPolicy, setMissPolicy }) {
    const [mapping, setMapping] = useState('SET');
    const [cacheSize, setCacheSize] = useState(1024);
    const [blockSize, setBlockSize] = useState(32);
    const [associativity, setAssociativity] = useState(4);
    const [policy, setPolicy] = useState('LRU');
    
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');

    useEffect(() => {
        if (mapping === 'DIRECT') {
            setAssociativity(1);
        } else if (mapping === 'FULLY') {
            setAssociativity(cacheSize / blockSize);
        } else if (mapping === 'SET') {
            if (!WAYS.includes(associativity)) setAssociativity(4);
        }
    }, [mapping, cacheSize, blockSize]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage('');

        let finalAssociativity = associativity;
        if (mapping === 'FULLY') {
            finalAssociativity = cacheSize / blockSize;
        }

        const url = `${API_BASE_URL}/config?cacheSizeBytes=${cacheSize}&blockSize=${blockSize}&associativity=${finalAssociativity}&replacementPolicy=${policy}`;
        
        try {
            const response = await fetch(url, { method: 'POST' });
            if (response.ok) {
                setMessage('Configuration applied successfully!😀');
                await fetchData(); 
            } else {
                const text = await response.text();
                setMessage(`Error: ${text}`);
            }
        } catch (error) {
            setMessage('Network Error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="config-form control-box">
            <h2 className="panel-title">⚙️ Cache Configuration</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Mapping Strategy:</label>
                    <select value={mapping} onChange={(e) => setMapping(e.target.value)} disabled={loading}>
                        {MAPPING_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                    </select>
                </div>

                <div className="form-row">
                    <label>
                        Cache Size (Bytes):
                        <select value={cacheSize} onChange={(e) => setCacheSize(Number(e.target.value))} disabled={loading}>
                            {CACHE_SIZES.map(s => <option key={s} value={s}>{s}</option>)}
                        </select>
                    </label>

                    <label>
                        Block Size (Bytes):
                        <select value={blockSize} onChange={(e) => setBlockSize(Number(e.target.value))} disabled={loading}>
                            {BLOCK_SIZES.map(s => <option key={s} value={s}>{s}</option>)}
                        </select>
                    </label>
                </div>

                <div className="form-group">
                    <label>Associativity (Ways):</label>
                    <select 
                        value={associativity} 
                        onChange={(e) => setAssociativity(Number(e.target.value))} 
                        disabled={loading || mapping !== 'SET'} 
                        className={mapping !== 'SET' ? 'disabled-input' : ''}
                    >
                        {mapping === 'DIRECT' && <option value={1}>1 (Direct Mapped)</option>}
                        {mapping === 'FULLY' && <option value={cacheSize/blockSize}>Full ({cacheSize/blockSize})</option>}
                        {mapping === 'SET' && WAYS.map(w => <option key={w} value={w}>{w}</option>)}
                    </select>
                </div>

                <div className="form-group">
                    <label>Replacement Policy:</label>
                    <select value={policy} onChange={(e) => setPolicy(e.target.value)} disabled={loading}>
                        <option value="LRU">LRU (Least Recently Used)</option>
                        <option value="FIFO">FIFO (First In First Out)</option>
                        <option value="RANDOM">Random</option>
                    </select>
                </div>

                <div className="form-group">
                    <label>Write Policy:</label>
                    <div className="radio-group">
                        <label>
                            <input 
                                type="radio" 
                                value="WRITE_BACK" 
                                checked={writePolicy === 'WRITE_BACK'} 
                                onChange={(e) => setWritePolicy(e.target.value)}
                                disabled={loading}
                            /> Write Back
                        </label>
                        <label>
                            <input 
                                type="radio" 
                                value="WRITE_THROUGH" 
                                checked={writePolicy === 'WRITE_THROUGH'} 
                                onChange={(e) => setWritePolicy(e.target.value)}
                                disabled={loading}
                            /> Write Through
                        </label>
                    </div>
                </div>

                <div className="form-group">
                    <label>Write Miss Policy:</label>
                    <div className="radio-group">
                        <label>
                            <input 
                                type="radio" 
                                value="WRITE_ALLOCATE" 
                                checked={missPolicy === 'WRITE_ALLOCATE'} 
                                onChange={(e) => setMissPolicy(e.target.value)}
                                disabled={loading}
                            /> Allocate
                        </label>
                        <label>
                            <input 
                                type="radio" 
                                value="NO_WRITE_ALLOCATE" 
                                checked={missPolicy === 'NO_WRITE_ALLOCATE'} 
                                onChange={(e) => setMissPolicy(e.target.value)}
                                disabled={loading}
                            /> No Allocate
                        </label>
                    </div>
                </div>
                
                <button type="submit" disabled={loading} className="btn-primary">
                    {loading ? 'Reconfiguring...' : 'Apply Configuration'}
                </button>
            </form>
            {message && <div className="status-msg">{message}</div>}
        </div>
    );
}

export default ConfigForm;