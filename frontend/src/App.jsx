import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import Footer from "./components/Footer";
import Home from "./pages/Home";
import Reservation from "./pages/Reservation";
import MySeats from "./pages/MySeats";
import Login from "./pages/Login";
import "./css/Common.css";

function App() {
  return (
    <Router>
      <div className="page-container">
        <Header />
        <main className="main">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/reservation" element={<Reservation />} />
            <Route path="/myseats" element={<MySeats />} />
            <Route path="/login" element={<Login />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
