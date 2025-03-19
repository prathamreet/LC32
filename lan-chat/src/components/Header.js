// components/Header.js
const Header = ({ nickname, status }) => {
    return (
        <header className="bg-blue-500 text-white p-4 flex justify-between items-center">
            <h1 className="text-xl font-bold">LAN Chat Application</h1>
            <div className="flex items-center space-x-4">
                {status && (
                    <div className="bg-blue-600 px-3 py-1 rounded text-sm">
                        Status: {status}
                    </div>
                )}
                {nickname && (
                    <div className="bg-blue-600 px-3 py-1 rounded">
                        Logged in as: <span className="font-semibold">{nickname}</span>
                    </div>
                )}
            </div>
        </header>
    );
};

export default Header;