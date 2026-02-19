import React from 'react';

function AccessHistory({ history }) {
    if (!history || history.length === 0) {
        return (
            <div className="history-panel control-box">
                <h3>🕐 Access History</h3>
                <p>Perform a READ or WRITE to view the log.</p>
            </div>
        );
    }

    return (
        <div className="history-panel control-box">
            <h3>🕒 Access History</h3>
            <table className="history-table">
                <thead>
                    <tr>
                        <th>Op</th>
                        <th>Addr</th>
                        <th>Tag</th>
                        <th>Set</th>
                        <th>Offset</th>
                        <th>Evicted</th>
                        <th>Wr Policy</th>
                        <th>Wr Miss Policy</th>
                    </tr>
                </thead>
                <tbody>
                    {history.map((access, index) => (
                        <tr key={index} className={access.hit ? 'history-hit' : 'history-miss'}>
                            <td>{access.operation}</td>
                            <td>{access.address}</td>
                            <td>{access.tag}</td>
                            <td>{access.setIndex}</td>
                            <td>{access.offset}</td>
                            <td>{access.evicted ? (
                                    <span>
                                        Evict Tag: {access.evictedTag}
                                        {access.evictedDirty}
                                    </span>
                                ) : ('-')}
                            </td>
                            <td>
                                <strong>{access.action}</strong> 
                                {access.operation === 'WRITE' && ` (${access.writePolicy})`}
                            </td>
                            <td>{access.missPolicy ? `${access.missPolicy}` : 'N/A'}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default AccessHistory;