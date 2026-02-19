import React, { useState, useEffect } from 'react';
import './App.css'; 
import AccessForm from './components/AccessForm';
import StatsPanel from './components/StatsPanel';
import ConfigForm from './components/ConfigForm';
import AccessHistory from './components/AccessHistory'; 
import CacheView from './components/CacheView';

const API_BASE_URL = 'http://localhost:8080/api/simulator';

function App() {
    const [cacheState, setCacheState] = useState(null);      
    const [stats, setStats] = useState(null);                    
    const [accessResult, setAccessResult] = useState(null);  
    const [accessHistory, setAccessHistory] = useState([]);
    const [writePolicy, setWritePolicy] = useState('WRITE_BACK');
    const [missPolicy, setMissPolicy] = useState('WRITE_ALLOCATE'); 

    const fetchAllData = async () => {
        try {
            const [stateRes, statsRes] = await Promise.all([
                fetch(`${API_BASE_URL}/state`),
                fetch(`${API_BASE_URL}/stats`),
            ]);
            
            if (!stateRes.ok || !statsRes.ok) {
               throw new Error('API request failed. Check server logs.');
            }
            
            const stateData = await stateRes.json();
            const statsData = await statsRes.json();

            setCacheState(stateData);
            setStats(statsData);
            
        } catch (error) {
            console.error("Failed to fetch initial data. Is Spring Boot running?", error);
        }
    };

    const handleAccessResponse = async (data) => {
        setAccessHistory(prevHistory => [
            {
                timestamp: new Date().toLocaleTimeString(),
                ...data
            },
            ...prevHistory
        ].slice(0, 10)); 

        setAccessResult({
            operation: data.operation,
            hit: data.hit,
            address: data.address,
            setIndex: data.setIndex, 
            tag: data.tag,
            blockIndex: data.wayIndex 
        });
        
        await fetchAllData(); 
    }

    useEffect(() => {
        fetchAllData();
    }, []);

    return (
        <div className="simulator-app-container">
            <h1 style={{textAlign: 'center', margin: '20px 0'}}>Cache Memory Simulator</h1>
            
            <div className="control-panel">
                <div className="left-column">
                    <ConfigForm
                        API_BASE_URL={API_BASE_URL}
                        fetchData={fetchAllData}
                        writePolicy={writePolicy}
                        setWritePolicy={setWritePolicy}
                        missPolicy={missPolicy}
                        setMissPolicy={setMissPolicy}
                    />

                    <AccessForm 
                        API_BASE_URL={API_BASE_URL}
                        handleAccessResponse={handleAccessResponse}
                        writePolicy={writePolicy}
                        missPolicy={missPolicy}
                    />
                </div>

                <div className="right-column">
                    <StatsPanel stats={stats} />
                    <AccessHistory history={accessHistory} />
                </div>
            </div> 
            
            <div className="visualization-area">
                {cacheState ? (
                    <CacheView 
                        cacheState={cacheState} 
                        accessResult={accessResult} 
                    />
                ) : (
                    <p style={{textAlign: 'center'}}>Connecting to Spring Boot backend and loading cache configuration...</p>
                )}
            </div>
        </div>
    );
}

export default App;