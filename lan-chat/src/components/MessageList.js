// components/MessageList.js
const MessageList = ({ messages }) => {
    return (
        <div className="flex-1 p-2 overflow-y-auto bg-white">
            {messages.map((msg, index) => {
                // Split the message into nickname and content parts
                const parts = msg.split(': ');
                const nickname = parts.length > 1 ? parts[0] : 'Unknown';
                const content = parts.length > 1 ? parts.slice(1).join(': ') : msg;
                
                return (
                    <div key={index} className="p-2 my-1 bg-gray-200 rounded-md">
                        <span className="font-bold">{nickname}</span>: {content}
                    </div>
                );
            })}
        </div>
    );
};

export default MessageList;