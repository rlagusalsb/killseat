import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import Footer from "./components/Footer";
import Home from "./pages/Home";
import MyReservation from "./pages/Reservations";
import Login from "./pages/Login";
import Performances from "./pages/Performance";
import PerformanceSeats from "./pages/PerformanceSeats";
import Board from "./pages/Board";
import SignUp from "./pages/SignUp";
import "./css/Common.css";

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
            <Route path="/performances/:performanceId/seats" element={<PerformanceSeats/>} />
            <Route path="/board" element={<Board />} />
            <Route path="/signup" element={<SignUp />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
