// components/MessageList.js
const MessageList = ({ messages, currentNickname }) => {
    return (
        <div className="flex-1 p-2 overflow-y-auto bg-white">
            {messages.map((msg, index) => {
                // Split the message into nickname and content parts
                const parts = msg.split(': ');
                const nickname = parts.length > 1 ? parts[0] : 'Unknown';
                const content = parts.length > 1 ? parts.slice(1).join(': ') : msg;
                
                // Determine if this is the current user's message
                const isCurrentUser = nickname === currentNickname;
                
                return (
                    <div 
                        key={index} 
                        className={`p-2 my-1 rounded-md ${
                            isCurrentUser 
                                ? 'bg-blue-100 text-blue-800 ml-auto mr-2 max-w-[80%]' 
                                : 'bg-gray-200 mr-auto ml-2 max-w-[80%]'
                        }`}
                    >
                        <span className="font-bold">{nickname}</span>: {content}
                    </div>
                );
            })}
        </div>
    );
};

export default MessageList;