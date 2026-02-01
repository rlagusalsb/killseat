import { Outlet, NavLink } from "react-router-dom";
import "../../css/admin/AdminLayout.css";

export default function AdminLayout() {
  return (
    <div className="admin-container">
      <aside className="admin-sidebar">
        <div className="admin-sidebar-title">관리자 메뉴</div>
        <nav className="admin-nav">
          <NavLink 
            to="/admin/members" 
            className={({ isActive }) => isActive ? "admin-nav-item active" : "admin-nav-item"}
          >
            회원 관리
          </NavLink>
          <NavLink 
            to="/admin/performances" 
            className={({ isActive }) => isActive ? "admin-nav-item active" : "admin-nav-item"}
          >
            공연 관리
          </NavLink>
          <NavLink 
            to="/admin/reservations" 
            className={({ isActive }) => isActive ? "admin-nav-item active" : "admin-nav-item"}
          >
            예매 관리
          </NavLink>
          <NavLink 
            to="/admin/payments" 
            className={({ isActive }) => isActive ? "admin-nav-item active" : "admin-nav-item"}
          >
            결제 관리
          </NavLink>
        </nav>
      </aside>
      
      <main className="admin-content">
        <Outlet />
      </main>
    </div>
  );
}