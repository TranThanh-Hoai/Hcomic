import React from "react";
import logo from "../assets/images/logo.svg";
import logo_2 from "../assets/images/logo_2.svg";
import banner from "../assets/images/banner_header.webp";

export const Header: React.FC = () => {
  return (
    <header className="relative h-20 overflow-hidden border-b border-gray-200 shadow-sm">

      {/* Background Banner */}
      <img
        src={banner}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-cover object-[center_25%] blur-[1px] pointer-events-none"
      />

      {/* Overlay */}
      <div className="absolute inset-0 bg-white/25 backdrop-blur-[1px]" />

      {/* Content */}
      <div className="relative z-10 flex h-full items-center justify-between px-6">

        {/* Logo */}
        <div className="flex items-center">
          <div className="flex items-center gap-3 rounded-xl bg-white/8 backdrop-blur-md px-3 py-2 shadow-sm">

            <img
              src={logo}
              alt="Hcomic Icon"
              className="w-8 h-8 object-contain"
            />

            <img
              src={logo_2}
              alt="Hcomic"
              className="h-7 w-auto object-contain"
            />

          </div>
        </div>

        {/* Right */}
        <div className="flex items-center gap-3">

          <button
            className="p-2 rounded-full bg-white/60 backdrop-blur-md hover:bg-white transition"
            aria-label="Search"
          >
            <svg
              className="w-5 h-5 text-gray-700"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
          </button>

          <button
            className="p-2 rounded-full bg-white/60 backdrop-blur-md hover:bg-white transition"
            aria-label="Menu"
          >
            <svg
              className="w-5 h-5 text-gray-700"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 6h16M4 12h16M4 18h16"
              />
            </svg>
          </button>

        </div>

      </div>
    </header>
  );
};

export default Header;