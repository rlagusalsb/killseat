import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Header from "./components/Header";
import Footer from "./components/Footer";
import Home from "./pages/Home";
import MyReservation from "./pages/MyReservations";
import Login from "./pages/Login";
import Performances from "./pages/Performance";
import PerformanceDetail from "./pages/PerformanceDetail";
import PerformanceSeats from "./pages/PerformanceSeats";
import Board from "./pages/Board";
import SignUp from "./pages/SignUp";
import PostWrite from "./pages/PostWrite";
import PostDetail from "./pages/PostDetail";
import PostEdit from "./pages/PostEdit";
import Waiting from "./pages/Waiting";
import AdminLayout from "./pages/admin/AdminLayout";
import AdminMemberList from "./pages/admin/AdminMemberList";
import AdminPaymentList from "./pages/admin/AdminPaymentList";
import AdminPerformanceList from "./pages/admin/AdminPerformanceList";
import AdminReservationList from "./pages/admin/AdminReservationList";
import "./css/Common.css";

const AdminRoute = ({ children }) => {
  const userRole = localStorage.getItem("role");
  if (userRole !== "ROLE_ADMIN") {
    alert("관리자 권한이 필요합니다.");
    return <Navigate to="/" />;
  }
  return children;
};

function App() {
  return (
    <Router>
      <div className="page-container">
        <Header />
        <main className="main">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/my-reservations" element={<MyReservation />} />
            <Route path="/login" element={<Login />} />
            <Route path="/performances" element={<Performances />} />
            <Route path="/performance/:id" element={<PerformanceDetail />} />
            <Route path="/performances/:performanceId/seats" element={<PerformanceSeats />} />
            <Route path="/board" element={<Board />} />
            <Route path="/signup" element={<SignUp />} />
            <Route path="/posts/write" element={<PostWrite />} />
            <Route path="/posts/:postId" element={<PostDetail />} />
            <Route path="/posts/:postId/edit" element={<PostEdit />} />
            <Route path="/waiting" element={<Waiting />} />

            <Route path="/admin" element={
              <AdminRoute>
                <AdminLayout />
              </AdminRoute>
            }>
              <Route index element={<AdminMemberList />} /> 
              <Route path="members" element={<AdminMemberList />} />
              <Route path="payments" element={<AdminPaymentList />} />
              <Route path="performances" element={<AdminPerformanceList />} />
              <Route path="reservations" element={<AdminReservationList />} />
            </Route>
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;