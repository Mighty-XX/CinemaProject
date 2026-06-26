-- Truy vấn của Phạm Minh Hùng - 202416928
-- 1. Thêm phim mới (INSERT)
-- Lưu ý: Định dạng ngày tháng trong PostgreSQL chuẩn là YYYY-MM-DD
EXPLAIN ANALYZE
INSERT INTO Movie (title, director, genre, duration, agerating, releasedate) 
VALUES ('Lật Mặt 7', 'Lý Hải', 'Gia đình', 120, 'K', '2024-04-26');

-- 2. Cập nhật thông tin phim (UPDATE)
EXPLAIN ANALYZE
UPDATE Movie 
SET duration = 131, agerating = 'T18' 
WHERE title = 'Lật mặt 7';

-- 3. Danh sách 10 khách hàng VIP
-- Sử dụng LIMIT để lấy Top 10 người có điểm số (rewardpoints) cao nhất
EXPLAIN ANALYZE
SELECT c.fullname, ma.username, ma.rewardpoints 
FROM Customer c
JOIN Movie_Account ma ON c.customerid = ma.customerid
ORDER BY ma.rewardpoints DESC 
LIMIT 10;

-- 4. Lọc các phim có chi phí bản quyền cao
EXPLAIN ANALYZE
SELECT m.title, m.genre, r.importprice 
FROM Movie m 
JOIN Receivinginvoice r ON m.movieid = r.movieid
WHERE r.importprice > 100000000 
ORDER BY r.importprice DESC;

-- 5. Lọc hóa đơn nhập do một Quản lý cụ thể phụ trách
EXPLAIN ANALYZE
SELECT * FROM Receivinginvoice 
WHERE importprice >= 200000000 
AND managerid = 2;

-- 6. Xóa phim (DELETE)
-- Lưu ý hệ thống: Cần đảm bảo phim này chưa có suất chiếu (ràng buộc khóa ngoại)
EXPLAIN ANALYZE
DELETE FROM Movie 
WHERE title = 'Lật mặt 7';

-- 7. Thống kê lượng vé bán theo phim
-- Kết nối 3 bảng Movie, MovieShow và Ticket để đếm số vé bán theo phim
EXPLAIN ANALYZE
SELECT m.title, COUNT(t.ticketid) AS "Tổng số vé đã bán"
FROM Movie m
JOIN MovieShow ms ON m.movieid = ms.movieid
JOIN Ticket t ON ms.movieshowid = t.movieshowid
GROUP BY m.title
ORDER BY COUNT(t.ticketid) DESC;

-- 8. Hiệu suất bán bắp/nước của nhân viên
-- Sử dụng hàm SUM() để cộng dồn số lượng sản phẩm F&B trong chi tiết hóa đơn
EXPLAIN ANALYZE
SELECT s.fullname, SUM(bd.quantity) AS "Tổng sản phẩm F&B đã bán"
FROM Staff s
JOIN Bill b ON s.staffid = b.staffid
JOIN Bill_Detail bd ON b.billid = bd.billid
GROUP BY s.fullname;

-- 9. Nhân viên không phát sinh giao dịch trong tháng hiện tại
-- Dùng NOT IN đối chiếu với tập hợp các staffid có lập hóa đơn trong tháng/năm nay
EXPLAIN ANALYZE
SELECT staffid, fullname 
FROM Staff 
WHERE staffid NOT IN (
    SELECT DISTINCT staffid 
    FROM Bill 
    WHERE EXTRACT(MONTH FROM issuedate) = EXTRACT(MONTH FROM CURRENT_DATE)
      AND EXTRACT(YEAR FROM issuedate) = EXTRACT(YEAR FROM CURRENT_DATE)
);

-- 10. Suất chiếu "cháy vé" (Đông khách nhất)
-- Dùng Subquery lồng trong mệnh đề HAVING để tìm ra con số MAX (số lượng vé lớn nhất)
EXPLAIN ANALYZE
SELECT ms.movieshowid, m.title, COUNT(t.ticketid) AS "Số vé bán ra"
FROM MovieShow ms
JOIN Movie m ON ms.movieid = m.movieid
JOIN Ticket t ON ms.movieshowid = t.movieshowid
GROUP BY ms.movieshowid, m.title
HAVING COUNT(t.ticketid) = (
    -- Subquery lấy ra số lượng vé cao nhất của một suất chiếu bất kỳ
    SELECT MAX(ticket_count)
    FROM (
        SELECT COUNT(ticketid) AS ticket_count
        FROM Ticket
        GROUP BY movieshowid
    ) AS Sub
);