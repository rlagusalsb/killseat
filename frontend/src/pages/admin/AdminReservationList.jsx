import { useEffect, useState } from "react";
import api from "../../api/client";
import "../../css/admin/AdminReservationList.css";

export default function AdminReservationList() {
  const [reservations, setReservations] = useState([]);

  useEffect(() => {
    api.get("/api/admin/reservations")
       .then(res => setReservations(res.data.content || res.data));
  }, []);

  return (
    <div className="admin-list-container">
      <h3>예매 내역 관리</h3>
      <table className="admin-table">
        <thead>
          <tr>
            <th>예매ID</th>
            <th>예매자</th>
            <th>공연명</th>
            <th>좌석</th>
            <th>상태</th>
          </tr>
        </thead>
        <tbody>
          {reservations.map(r => (
            <tr key={r.reservationId}>
              <td>{r.reservationId}</td>
              <td>{r.memberName}</td>
              <td>{r.performanceTitle}</td>
              <td>{r.seatInfo}</td>
              <td>{r.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}