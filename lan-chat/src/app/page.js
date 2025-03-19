"use client"; // Ensure this is a client component

import { useState, useEffect } from "react";
import Header from "../components/Header";
import ChatWindow from "../components/ChatWindow";

export default function Home() {
    const [messages, setMessages] = useState([]);
    const [nickname, setNickname] = useState("");
    const [isConnected, setIsConnected] = useState(false);
    const [error, setError] = useState(null);
    const [apiStatus, setApiStatus] = useState("Connecting to Java backend...");

    // Connect to the Java API server
    useEffect(() => {
        if (!nickname) {
            const userNickname = prompt("Enter your nickname:");
            if (userNickname) {
                setNickname(userNickname);
                setIsConnected(true);
                setApiStatus("Connected. Waiting for messages...");
            } else {
                alert("Nickname is required to join the chat.");
            }
        }
    }, [nickname]);

    // Fetch messages periodically
    useEffect(() => {
        if (!isConnected) return;

        const fetchMessages = async () => {
            try {
                setError(null);
                const response = await fetch('http://localhost:8080/api/messages');
                
                if (response.ok) {
                    const data = await response.json();
                    setMessages(data);
                    setApiStatus("Connected. Receiving messages...");
                } else {
                    throw new Error(`API returned status: ${response.status}`);
                }
            } catch (error) {
                console.error('Error fetching messages:', error);
                setError(`Cannot connect to Java backend. Make sure it's running.`);
                setApiStatus("Disconnected. Check Java backend.");
            }
        };

        // Initial fetch
        fetchMessages();

        // Set up polling every 2 seconds (increased to reduce load)
        const intervalId = setInterval(fetchMessages, 2000);

        return () => clearInterval(intervalId);
    }, [isConnected]);

    const handleSendMessage = async (message) => {
        if (!isConnected || !message.trim()) return;

        try {
            setError(null);
            const formattedMessage = `${nickname}: ${message}`;
            
            const response = await fetch('http://localhost:8080/api/sendMessage', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formattedMessage),
            });
            
            if (!response.ok) {
                throw new Error(`Failed to send message: ${response.status}`);
            }
        } catch (error) {
            console.error('Error sending message:', error);
            setError(`Cannot send message. Check connection to Java backend.`);
        }
    };

    return (
        <div className="h-screen flex flex-col">
            <Header nickname={nickname} status={apiStatus} />
            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                    <strong className="font-bold">Error: </strong>
                    <span className="block sm:inline">{error}</span>
                </div>
            )}
            <ChatWindow messages={messages} onSendMessage={handleSendMessage} />
        </div>
    );
}