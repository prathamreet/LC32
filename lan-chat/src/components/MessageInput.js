// app/components/MessageInput.js
import { useState } from 'react';

const MessageInput = ({ onSendMessage }) => {
    const [message, setMessage] = useState('');

    const handleSend = () => {
        if (message.trim()) {
            onSendMessage(message);
            setMessage('');
        }
    };

    return (
        <div className="flex p-2 bg-gray-100">
            <input
                type="text"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                placeholder="Type your message..."
                className="flex-1 p-2 border border-gray-300 rounded-l-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <button
                onClick={handleSend}
                className="ml-2 px-4 py-2 bg-blue-500 text-white rounded-r-md hover:bg-blue-600"
            >
                Send
            </button>
        </div>
    );
};

export default MessageInput;