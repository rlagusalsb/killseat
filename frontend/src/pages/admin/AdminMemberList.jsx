import { useEffect, useState } from "react";
import api from "../../api/client";
import "../../css/admin/AdminMemberList.css";

export default function AdminMemberList() {
  const [members, setMembers] = useState([]);

  useEffect(() => {
    api.get("/api/admin/members")
       .then(res => setMembers(res.data.content || res.data))
       .catch(err => console.error("회원 목록 로딩 실패:", err));
  }, []);

  return (
    <div className="admin-list-container">
        <h3>회원 명단 관리</h3>
        <table className="admin-table">
        <thead>
            <tr>
            <th>ID</th>
            <th>이메일</th>
            <th>이름</th>
            <th>권한</th>
            <th>가입일</th>
            </tr>
        </thead>
        <tbody>
            {members.map(m => (
            <tr key={m.memberId}>
                <td>{m.memberId}</td>
                <td>{m.email}</td>
                <td>{m.name}</td>
                <td>
                <span className={`role-badge ${m.role}`}>
                    {m.role}
                </span>
                </td>
                <td>{new Date(m.createdAt).toLocaleDateString()}</td>
            </tr>
            ))}
        </tbody>
        </table>
    </div>
    );
}