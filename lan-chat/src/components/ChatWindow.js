// app/components/ChatWindow.js
import MessageList from './MessageList';
import MessageInput from './MessageInput';

const ChatWindow = ({ messages, onSendMessage, nickname }) => {
    return (
        <div className="flex flex-col h-full border border-gray-300 rounded-lg overflow-hidden">
            <MessageList messages={messages} currentNickname={nickname} />
            <MessageInput onSendMessage={onSendMessage} />
        </div>
    );
};

export default ChatWindow;