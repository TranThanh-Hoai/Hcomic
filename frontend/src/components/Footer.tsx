import React from 'react';
import bannerFooter from '../assets/images/banner_footer.webp';
import logo from '../assets/images/logo.svg';
import logo_2 from '../assets/images/logo_2.svg';

export const Footer: React.FC = () => {
  return (
    <footer className="relative bg-gray-900 text-gray-200 overflow-hidden border-t border-gray-800">
      {/* Background Banner Image (Lớp dưới cùng) */}
      <img
        src={bannerFooter}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-cover object-center pointer-events-none"
      />

      {/* Overlay mờ làm nổi bật nội dung */}
      <div className="absolute inset-0 bg-slate-950/85 backdrop-blur-[2px]" />

      {/* Content */}
      <div className="relative z-10 max-w-7xl mx-auto px-6 py-8 md:py-10 flex flex-col gap-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-start">
          {/* Cột 1: Logo & Giới thiệu */}
          <div className="flex flex-col gap-3">
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-3 rounded-xl bg-white/10 backdrop-blur-md px-3 py-2 shadow-sm">
                <img
                  src={logo}
                  alt="Hcomic Icon"
                  className="w-7 h-7 object-contain"
                />
                <img
                  src={logo_2}
                  alt="Hcomic"
                  className="h-6 w-auto object-contain brightness-0 invert"
                />
              </div>
            </div>
            <p className="text-sm text-gray-300 leading-relaxed mt-1">
              Hcomic - Nền tảng đọc truyện tranh trực tuyến trải nghiệm mượt mà, phong phú và cập nhật liên tục.
            </p>
          </div>

          {/* Cột 2: Thông tin cá nhân & Địa chỉ */}
          <div className="flex flex-col gap-2 text-sm text-gray-300">
            <h3 className="font-semibold text-white text-base mb-1">Thông tin liên hệ</h3>
            <p><span className="text-gray-400">Chủ web:</span> H</p>
            <p><span className="text-gray-400">Địa chỉ:</span> 123</p>
            <p><span className="text-gray-400">Email:</span> contact@hcomic.com</p>
          </div>

          {/* Cột 3: Giới thiệu & Điều khoản */}
          <div className="flex flex-col gap-2 text-sm text-gray-300">
            <h3 className="font-semibold text-white text-base mb-1">Về Hcomic</h3>
            <p className="text-xs text-gray-400 leading-relaxed">
              Tất cả các nội dung truyện tranh trên nền tảng được tổng hợp và chia sẻ vì mục đích giải trí và học tập.
            </p>
          </div>
        </div>

        {/* Đường kẻ ngang và Copyright */}
        <div className="border-t border-white/15 pt-4 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 gap-2">
          <p>© {new Date().getFullYear()} Hcomic. All rights reserved.</p>
          <p className="text-gray-400">Phát triển bởi H</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

