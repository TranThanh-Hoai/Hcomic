import React from 'react';
import Header from '../components/Header';
import Footer from '../components/Footer';

export const Home: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col bg-white">
      {/* Header ở trên */}
      <Header />

      {/* Main Content (Body) ở giữa, chiếm toàn bộ chiều cao còn lại */}
      <main className="flex-1 bg-white">
        {/* Vùng hiển thị nội dung trống theo yêu cầu */}
      </main>

      {/* Footer ở dưới */}
      <Footer />
    </div>
  );
};

export default Home;
