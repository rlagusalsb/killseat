import { useEffect, useState } from "react";
import api from "../../api/client";
import "../../css/admin/AdminPaymentList.css";

export default function AdminPaymentList() {
  const [payments, setPayments] = useState([]);

  useEffect(() => {
    api.get("/api/admin/payments")
       .then(res => setPayments(res.data.content || res.data));
  }, []);

  return (
    <div className="admin-list-container">
      <div className="admin-header">
        <h3>결제 내역 관리</h3>
      </div>
      <div className="admin-table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <th style={{ width: "80px" }}>결제ID</th>
              <th style={{ width: "180px" }}>결제자</th>
              <th>공연 정보</th>
              <th style={{ width: "120px" }}>금액</th>
              <th style={{ width: "100px" }}>상태</th>
              <th style={{ width: "180px" }}>결제일</th>
            </tr>
          </thead>
          <tbody>
            {payments.map(p => (
              <tr key={p.paymentId} className={p.status === "CANCELED" ? "row-canceled" : ""}>
                <td>{p.paymentId}</td>
                <td>
                  <div className="cell-buyer">
                    <span className="buyer-name">{p.buyerName}</span>
                    <span className="buyer-email">{p.buyerEmail}</span>
                  </div>
                </td>
                <td>
                  <div className="cell-performance">
                    <span className="perf-title" title={p.performanceTitle}>
                      {p.performanceTitle}
                    </span>
                    <span className="perf-round">{p.performanceRound}</span>
                  </div>
                </td>
                <td className="amount-cell">
                  {p.amount?.toLocaleString()}원
                </td>
                <td>
                  <span className={`status-badge ${p.status}`}>
                    {p.status === "SUCCESS" ? "결제완료" : p.status === "CANCELED" ? "취소됨" : p.status}
                  </span>
                </td>
                <td className="date-cell">
                  {new Date(p.createdAt).toLocaleString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}