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
      <h3>결제 내역 관리</h3>
      <table className="admin-table">
        <thead>
          <tr>
            <th>결제ID</th>
            <th>주문번호</th>
            <th>금액</th>
            <th>상태</th>
            <th>결제일</th>
          </tr>
        </thead>
        <tbody>
          {payments.map(p => (
            <tr key={p.paymentId}>
              <td>{p.paymentId}</td>
              <td>{p.impUid}</td>
              <td className="amount">{p.amount?.toLocaleString()}원</td>
              <td><span className={`status-badge ${p.status}`}>{p.status}</span></td>
              <td>{new Date(p.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}