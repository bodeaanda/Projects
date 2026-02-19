import React from 'react';

function StatsPanel({ stats }) {
    if (!stats) {
        return <p>Statistics not available.</p>;
    }

    const totalAccesses = stats.reads + stats.writes;
    const hitRate = stats.hits / (stats.hits + stats.misses) * 100 || 0;
    const missRate = stats.misses / (stats.hits + stats.misses) * 100 || 0;

    return (
        <div className="stats-panel">
            <h3>📊 Simulation Statistics</h3>
            <table className="stats-table">
                <tbody>
                    <tr><th>Total Accesses</th><td>{totalAccesses}</td></tr>
                    <tr><th>Cache Hits</th><td>{stats.hits}</td></tr>
                    <tr><th>Cache Misses</th><td>{stats.misses}</td></tr>
                    <tr><th>Read Operations</th><td>{stats.reads}</td></tr>
                    <tr><th>Write Operations</th><td>{stats.writes}</td></tr>
                    <tr className="rate-row"><th>Hit Rate</th><td>{hitRate.toFixed(2)}%</td></tr>
                    <tr className="rate-row"><th>Miss Rate</th><td>{missRate.toFixed(2)}%</td></tr>
                </tbody>
            </table>
        </div>
    );
}

export default StatsPanel;