import React from 'react';
import './CacheView.css';

const formatData = (inputData) => {
    if (!inputData) return '00 00...';
    let dataArray = [];
    if (typeof inputData === 'string') {
        try {
            const binaryString = atob(inputData);
            const len = binaryString.length;
            const bytes = new Uint8Array(len);
            for (let i = 0; i < len; i++) {
                bytes[i] = binaryString.charCodeAt(i);
            }
            dataArray = Array.from(bytes);
        } catch (e) {
            console.error("Decoding error:", e);
            return "ERR";
        }
    } else if (Array.isArray(inputData)) {
        dataArray = inputData;
    }
    if (dataArray.length === 0) return '00 00...';
    const displayBytes = dataArray.slice(0, 4);
    const hexString = displayBytes.map(byte => {
        const positiveByte = byte & 0xFF;
        return positiveByte.toString(16).padStart(2, '0').toUpperCase();
    }).join(' ');
    return hexString + (dataArray.length > 4 ? '...' : '');
};

const getRawDataArray = (inputData) => {
    if (!inputData) return [];
    
    if (typeof inputData === 'string') {
        try {
            const binaryString = atob(inputData);
            const len = binaryString.length;
            const bytes = new Uint8Array(len);
            for (let i = 0; i < len; i++) {
                bytes[i] = binaryString.charCodeAt(i);
            }
            return Array.from(bytes);
        } catch (e) {
            return [];
        }
    } else if (Array.isArray(inputData)) {
        return inputData;
    }
    return [];
};

function CacheView({ cacheState, accessResult }) {
 
    if (!cacheState) {
        return <div className="cache-container"><p>Waiting for cache state...</p></div>;
    }

    const setsData = cacheState.sets || cacheState.cacheSets || cacheState.data || [];
    const nrSets = cacheState.nrSets || cacheState.numSets || 0;
    const associativity = cacheState.associativity || 1;
    const blockSize = cacheState.blockSize || 4;

    if (!Array.isArray(setsData) || setsData.length === 0) {
        return (
            <div className="cache-container" style={{ border: '2px solid red' }}>
                <h3 style={{ color: 'red' }}>Structure Mismatch</h3>
                <p>Sets array is missing.</p>
            </div>
        );
    }

    return (
        <div className="cache-container">
            <h3>Cache Memory Visualization</h3>
            
            <div className="cache-info-bar">
                 <div className="info-item">
                    <span className="info-label">Cache Type:</span>
                    <span className="info-value">
                        {associativity === 1 ? 'Direct Mapped' : 
                         associativity === nrSets ? 'Fully Associative' : 
                         `${associativity}-Way Set Associative`}
                    </span>
                </div>
                <div className="info-item"><span className="info-label">Sets:</span><span className="info-value">{nrSets}</span></div>
                <div className="info-item"><span className="info-label">Associativity:</span><span className="info-value">{associativity}</span></div>
                <div className="info-item"><span className="info-label">Block Size:</span><span className="info-value">{blockSize} B</span></div>
            </div>

            {accessResult && (
                <div className="instruction-breakdown">
                    <h4>Instruction Breakdown</h4>
                    <div className="breakdown-grid">
                        <div className="breakdown-item">
                            <span className="breakdown-label">Address:</span>
                            <span className="breakdown-value">0x{accessResult.address?.toString(16).toUpperCase()}</span>
                        </div>
                        <div className="breakdown-item">
                            <span className="breakdown-label">Set Index:</span>
                            <span className="breakdown-value">{accessResult.setIndex}</span>
                        </div>
                        <div className="breakdown-item">
                            <span className="breakdown-label">Tag:</span>
                            <span className="breakdown-value">{accessResult.tag}</span>
                        </div>
                        <div className="breakdown-item">
                            <span className="breakdown-label">Result:</span>
                            <span className={`breakdown-value ${accessResult.hit ? 'hit' : 'miss'}`}>
                                {accessResult.hit ? 'HIT' : 'MISS'}
                            </span>
                        </div>
                    </div>
                </div>
            )}

            <div className="cache-table-container">
                <h4>Cache Table</h4>
                <table className="cache-table">
                    <thead>
                        <tr>
                            <th>Set Index</th>
                            {associativity > 1 && <th>Way</th>}
                            <th>Valid</th>
                            <th>Dirty</th>
                            <th>Tag</th>
                            <th>Data (Hex)</th>
                        </tr>
                    </thead>
                    <tbody>
                        {setsData.map((set, setIndex) => {
                            const blocks = set.cacheBlocks || set.blocks || []; 
                            const rows = [];
                            
                            for (let way = 0; way < associativity; way++) {
                                const block = blocks[way] || { valid: false, dirty: false, tag: '-', data: [] };
                                
                                const isAccessed = accessResult && 
                                                   accessResult.setIndex === setIndex && 
                                                   accessResult.blockIndex === way; 
                                
                                const highlightClass = isAccessed 
                                    ? (accessResult.hit ? 'highlight-hit' : 'highlight-miss')
                                    : '';
                                
                                rows.push(
                                    <tr key={`${setIndex}-${way}`} className={highlightClass}>
                                        {way === 0 && (
                                            <td rowSpan={associativity} className="set-index-cell">
                                                Set {setIndex}
                                            </td>
                                        )}
                                        {associativity > 1 && (
                                            <td className="way-cell">Way {way}</td>
                                        )}
                                        <td className={`valid-cell ${block.valid ? 'valid' : 'invalid'}`}>
                                            {block.valid ? '1' : '0'}
                                        </td>
                                        <td className={`dirty-cell ${block.dirty ? 'dirty' : 'clean'}`}>
                                            {block.dirty ? '1' : '0'}
                                        </td>
                                        <td className="tag-cell">{block.valid ? block.tag : '-'}</td>
                                        <td className="data-cell">
                                            {block.valid ? formatData(block.data) : 'FREE'}
                                        </td>
                                    </tr>
                                );
                            }
                            return rows;
                        })}
                    </tbody>
                </table>
            </div>
      
             {accessResult && (
                <div className="memory-block-visualization">
                    <h4>Memory Block</h4>
                    
                    {(() => {
                        const activeSet = setsData[accessResult.setIndex];
                        const activeBlock = activeSet ? (activeSet.cacheBlocks || activeSet.blocks || [])[accessResult.blockIndex] : null;
                        
                        let rawData = [];
                        if (activeBlock && activeBlock.valid) {
                            const blockData = getRawDataArray(activeBlock.data);
                            rawData = blockData.length > 0 
                                ? [...blockData, ...Array(Math.max(0, blockSize - blockData.length)).fill(0)].slice(0, blockSize)
                                : Array(blockSize).fill(0);
                        } else {
                            rawData = Array(blockSize).fill(0);
                        }

                        const offset = accessResult.address % blockSize;

                        return (
                            <div className="memory-block">
                                <div className="memory-block-header">
                                    <strong>Block Data: </strong> Set {accessResult.setIndex}, Way {accessResult.blockIndex}
                                    <br/>
                                    <small style={{color: '#465775'}}>
                                        (Address 0x{accessResult.address?.toString(16).toUpperCase()} is at Offset {offset})
                                    </small>
                                </div>

                                <div className="memory-block-content">
                                    {rawData.map((byteVal, i) => (
                                        <div 
                                            key={i} 
                                            className="memory-byte"
                                            style={i === offset ? {
                                                border: '2px solid #EF6F6C', 
                                                transform: 'scale(1.1)', 
                                                backgroundColor: '#fff',
                                                boxShadow: '0 0 10px rgba(239, 111, 108, 0.5)'
                                            } : {}}
                                        >
                                            <div className="byte-index" style={{fontSize: '10px', color: '#888'}}>
                                                {i.toString(16).toUpperCase()}
                                            </div>
                                            <div className="byte-value">
                                                {byteVal.toString(16).padStart(2, '0').toUpperCase()}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        );
                    })()}
                </div>
            )}
        </div>
    );
}

export default CacheView;