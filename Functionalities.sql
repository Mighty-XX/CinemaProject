--
-- Khung nhìn (VIEW)
--
--Tạo View Báo cáo doanh thu ngày (v_Daily_Revenue)
-- Sử dụng CREATE OR REPLACE để tự động ghi đè/cập nhật nếu View này đã từng được tạo trước đó
CREATE OR REPLACE VIEW v_Daily_Revenue AS
SELECT 
    -- Cột issuedate gốc là kiểu TIMESTAMP (ví dụ: '2024-05-10 14:30:00'). 
    -- Dùng cú pháp ::DATE để cắt bỏ phần giờ phút, ép về kiểu DATE (chỉ còn '2024-05-10') giúp việc gom nhóm chính xác hơn.
    issuedate::DATE AS ngay_giao_dich,
    
    -- Đếm tổng số lượng hóa đơn (mỗi billid là 1 hóa đơn) được bán ra trong ngày
    COUNT(billid) AS tong_so_hoa_don,
    
    -- Dùng hàm SUM cộng dồn toàn bộ cột totalamount để ra tổng số tiền thu được
    SUM(totalamount) AS tong_doanh_thu
FROM Bill
-- Lệnh GROUP BY bắt buộc phải có khi dùng COUNT/SUM. Nó sẽ gom tất cả các hóa đơn có cùng chung 1 ngày lại thành 1 dòng để thống kê.
GROUP BY issuedate::DATE
-- Sắp xếp dữ liệu theo ngày giao dịch giảm dần (DESC), ngày nào mới nhất (hôm nay) sẽ hiện lên trên cùng.
ORDER BY ngay_giao_dich DESC;
-- Cách kiểm tra kết quả:
-- SELECT * FROM v_Daily_Revenue;


-- Tạo View Thông tin Lịch chiếu (v_Public_Showtime)
-- Mục đích của View này là để đẩy dữ liệu lên trang web cho khách hàng xem. 
-- Do đó, ta tuyệt đối không SELECT các cột khóa chính/khóa ngoại (movieid, roomid...) để tránh bị hacker dò rỉ cấu trúc DB.
CREATE OR REPLACE VIEW v_Public_Showtime AS
SELECT 
    m.title AS ten_phim,
    r.roomname AS phong_chieu,
    s.showdate AS ngay_chieu,
    s.starttime AS gio_bat_dau
FROM MovieShow ms
-- Bảng MovieShow (Bảng trung tâm) chỉ chứa các con số ID. Ta phải dùng lệnh JOIN nối với 3 bảng cha để lấy ra chữ/thông tin thực tế.
JOIN Movie m ON ms.movieid = m.movieid           -- Nối với bảng Movie để bốc được tên phim (title)
JOIN Room r ON ms.roomid = r.roomid              -- Nối với bảng Room để bốc được tên phòng (roomname)
JOIN Showtime s ON ms.showtimeid = s.showtimeid  -- Nối với bảng Showtime để bốc được ngày và giờ chiếu
-- Sắp xếp ưu tiên: Ngày chiếu tăng dần (ASC - Ngày sớm hiện trước), sau đó đến Giờ chiếu tăng dần (ASC - Ca sáng hiện trước ca chiều).
ORDER BY s.showdate ASC, s.starttime ASC;


--
-- Chỉ mục (INDEX)
--
-- Tạo Index tối ưu tìm kiếm (idx_customer_fullname)
-- Lệnh CREATE INDEX tạo ra một tệp chỉ mục chạy ngầm dưới hệ thống cho riêng cột fullname.
-- Nó hoạt động giống như "Mục lục" ở những trang cuối của một cuốn sách dày.
CREATE INDEX idx_customer_fullname 
ON Customer (fullname);
-- Tác dụng: Bất cứ khi nào thu ngân dùng lệnh tìm kiếm: SELECT * FROM Customer WHERE fullname = '...',
-- PostgreSQL sẽ lật thẳng vào trang "Mục lục" này để chỉ ra luôn khách hàng đang nằm ở dòng số mấy, thay vì phải quét dò từng dòng từ trên xuống dưới toàn bộ bảng Customer. Tốc độ tìm kiếm sẽ tăng lên gấp nhiều lần.


-- Tạo Index tối ưu công cụ tìm kiếm phim (idx_movie_search)
-- Tạo một Composite Index (Chỉ mục kết hợp đa cột) trên cả 2 cột title và genre
CREATE INDEX idx_movie_search 
ON Movie (title, genre);
-- Tác dụng: Khi khách hàng dùng bộ lọc trên App (Ví dụ: Tìm phim có chữ "Lật Mặt" VÀ thuộc thể loại "Hành động" -> SELECT * FROM Movie WHERE title LIKE '...' AND genre = '...'), 


-- Tối ưu hóa các lệnh JOIN trung tâm (B-Tree Index trên Khóa ngoại)
-- Bảng MovieShow là "trái tim" của hệ thống (nối Phim, Phòng, Giờ chiếu). 
-- Việc tra cứu "Phim A có lịch chiếu nào?" diễn ra liên tục.
CREATE INDEX IF NOT EXISTS idx_movieshow_movie_showtime 
ON MovieShow (movieid, showtimeid);


-- Tối ưu hóa Báo cáo Thống kê & Doanh thu (Index có sắp xếp DESC)
-- Quản lý luôn muốn xem doanh thu và hóa đơn MỚI NHẤT. 
-- Index này giúp câu lệnh thống kê gom nhóm theo ngày (GROUP BY issuedate) chạy nhanh gấp nhiều lần.
CREATE INDEX IF NOT EXISTS idx_bill_issuedate 
ON Bill (issuedate DESC);


-- Tối ưu hóa sơ đồ ghế và chống bán trùng (Composite Index)
-- Khi chọn suất chiếu, hệ thống phải quét bảng Ticket để xem ghế nào đã bán. 
-- Index phức hợp này giúp App nảy ra sơ đồ ghế ngay lập tức, đồng thời hỗ trợ Trigger Chống bán trùng ghế chạy mượt hơn.
CREATE INDEX IF NOT EXISTS idx_ticket_seat_check 
ON Ticket (movieshowid, seatid);


-- Bật tính năng tìm kiếm Full-Text Search (GIN Index)
-- Flex kỹ năng Database nâng cao: Hỗ trợ thanh tìm kiếm (Search bar) trên App khách hàng.
-- Khách gõ "lat mat" hay "LẬT mặt" thì hệ thống vẫn dịch ra vector từ khóa tiếng Việt để tìm đúng phim.
CREATE INDEX IF NOT EXISTS idx_movie_title_fts 
ON Movie USING gin(to_tsvector('simple', title));

-- CÁC IDX KHÁC
CREATE INDEX idx_account_points ON Movie_Account(rewardpoints DESC);

-- Tối ưu nối bảng Phim và Suất chiếu
CREATE INDEX idx_movieshow_movie ON MovieShow(movieid);

-- Tối ưu nối bảng Suất chiếu và Vé (Cực kỳ quan trọng vì bảng Ticket sẽ chứa nhiều dữ liệu nhất)
CREATE INDEX idx_ticket_movieshow ON Ticket(movieshowid);

-- Tối ưu nối bảng Hóa đơn (Bill) với Nhân viên (Staff)
CREATE INDEX idx_bill_staff ON Bill(staffid);

-- Tạo Index trực tiếp trên kết quả của hàm trích xuất Tháng và Năm
CREATE INDEX idx_bill_month_year 
ON Bill (EXTRACT(YEAR FROM issuedate), EXTRACT(MONTH FROM issuedate));

--
-- Hàm (FUNCTION) 
--
-- FUNCTION 1: Tính tuổi hiện tại của khách hàng
CREATE OR REPLACE FUNCTION fn_CalculateAge(p_customerid INT)
RETURNS INT AS $$
DECLARE
    v_age INT; -- Khai báo biến cục bộ để lưu trữ kết quả tuổi tính được
BEGIN
    -- Sử dụng hàm AGE() để tính khoảng thời gian từ ngày sinh (dob) đến hiện tại (CURRENT_DATE)
    -- Hàm EXTRACT(YEAR FROM ...) giúp "bóc" lấy đúng con số năm (tuổi) từ khoảng thời gian đó
    -- Từ khóa INTO dùng để gán kết quả vừa truy vấn được vào biến v_age
    SELECT EXTRACT(YEAR FROM AGE(CURRENT_DATE, dob)) INTO v_age
    FROM Customer
    WHERE customerid = p_customerid;

    -- Trả kết quả cuối cùng về cho người gọi hàm
    RETURN v_age;
END;
$$ LANGUAGE plpgsql;
-- Hướng dẫn test hàm:
-- SELECT fn_CalculateAge(1); -- Tính tuổi của khách hàng có ID = 1


-- FUNCTION 2: Thống kê tổng doanh thu bán vé của một bộ phim
CREATE OR REPLACE FUNCTION fn_CalculateMovieRevenue(p_movieid INT)
RETURNS NUMERIC AS $$
DECLARE
    v_total_revenue NUMERIC; -- Khai báo biến lưu trữ tổng doanh thu (kiểu số thực)
BEGIN
    -- Truy vấn để tính tổng tiền
    SELECT COALESCE(SUM(t.price), 0) INTO v_total_revenue
    FROM Ticket t
    -- Liên kết vé với suất chiếu để biết vé này thuộc về suất chiếu nào
    JOIN MovieShow ms ON t.movieshowid = ms.movieshowid
    -- Điều kiện lọc: Chỉ lấy những suất chiếu của đúng bộ phim đang được truyền vào hàm
    WHERE ms.movieid = p_movieid;

    -- Trả kết quả doanh thu về
    RETURN v_total_revenue;
END;
$$ LANGUAGE plpgsql;
-- Hướng dẫn test hàm:
-- SELECT fn_CalculateMovieRevenue(101); -- Xem tổng doanh thu của phim Lật Mặt 6 (giả sử ID = 101)

--
-- Trình kích hoạt (TRIGGER)
--
-- Tạo TRIGGER - Chống bán trùng ghế (Double Booking): Viết Trigger kích hoạt trước khi INSERT vào bảng Ticket. 
-- Ràng buộc kiểm tra: Nếu cặp giá trị (movieshowid, seatid) đã tồn tại trong bảng Ticket thì báo lỗi báo 
-- "Ghế này đã có người đặt!".
-- Bước 1: Viết logic kiểm tra trùng lặp
CREATE OR REPLACE FUNCTION fn_PreventDoubleBooking()
RETURNS TRIGGER AS $$
BEGIN
    -- Kiểm tra xem đã có dòng nào trong bảng Ticket trùng khớp cả mã suất chiếu và mã ghế chưa
    IF EXISTS (
        SELECT 1 FROM Ticket 
        WHERE movieshowid = NEW.movieshowid AND seatid = NEW.seatid
    ) THEN
        -- Bắn lỗi và tự động Rollback (hủy) giao dịch
        RAISE EXCEPTION 'Lỗi giao dịch: Ghế này đã có người đặt!';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Bước 2: Gắn Trigger vào bảng Ticket 
CREATE TRIGGER trg_Before_Insert_Ticket_DoubleBooking
BEFORE INSERT ON Ticket
FOR EACH ROW
EXECUTE FUNCTION fn_PreventDoubleBooking();


-- Tạo TRIGGER - Kiểm soát giảm giá (Discount Audit): Viết Trigger kích hoạt trên bảng Bill. 
-- Kiểm tra tính hợp lệ: Số tiền giảm giá (discountamount) tuyệt đối không được phép lớn hơn hoặc bằng tổng tiền
-- thanh toán (totalamount). Nếu vi phạm, tự động Rollback giao dịch.
CREATE OR REPLACE FUNCTION fn_DiscountAudit()
RETURNS TRIGGER AS $$
BEGIN
    -- Kiểm tra nếu số tiền giảm giá lớn hơn hoặc bằng tổng tiền (sau khi đã khấu trừ)
    -- Sử dụng COALESCE để đề phòng trường hợp discountamount bị bỏ trống (NULL)
    IF COALESCE(NEW.discountamount, 0) >= NEW.totalamount THEN
        RAISE EXCEPTION 'Lỗi nghiệp vụ: Số tiền giảm giá không được lớn hơn hoặc bằng tổng tiền thanh toán!';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_Before_Insert_Update_Bill_Discount
BEFORE INSERT OR UPDATE ON Bill
FOR EACH ROW
EXECUTE FUNCTION fn_DiscountAudit();


-- Trigger Tự động cộng điểm tích lũy: Viết một Trigger gán vào bảng Bill. Mỗi khi một hóa đơn mới được INSERT,
--  hệ thống tự động tính điểm thưởng (ví dụ: 10,000 VNĐ = 1 điểm) từ totalamount và cộng dồn vào rewardpoints
-- của khách hàng tương ứng trong bảng Movie_Account.
CREATE OR REPLACE FUNCTION fn_AddRewardPoints()
RETURNS TRIGGER AS $$
DECLARE
    v_points INT;
BEGIN
    -- Quy đổi: 10,000 VNĐ = 1 điểm. Làm tròn xuống bằng hàm FLOOR
    v_points := FLOOR(NEW.totalamount / 10000);

    -- Chỉ cộng điểm nếu khách hàng này có đăng ký thẻ thành viên (customerid NOT NULL)
    IF v_points > 0 AND NEW.customerid IS NOT NULL THEN
        UPDATE Movie_Account
        SET rewardpoints = COALESCE(rewardpoints, 0) + v_points
        WHERE customerid = NEW.customerid;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger này chạy SAU KHI hóa đơn đã được chèn thành công
CREATE TRIGGER trg_After_Insert_Bill_Points
AFTER INSERT ON Bill
FOR EACH ROW
EXECUTE FUNCTION fn_AddRewardPoints();


-- Trigger Tự động nâng hạng thành viên: Viết Trigger bắt sự kiện cập nhật trên bảng Movie_Account. 
-- Nếu rewardpoints vượt mốc 2000 điểm -> UPDATE membershiplevel lên 'Bạc'; vượt 5000 -> 'Vàng'; 
-- vượt 10000 -> 'Kim Cương', <2000 -> 'Đồng'.
CREATE OR REPLACE FUNCTION fn_UpgradeMembership()
RETURNS TRIGGER AS $$
BEGIN
    -- Logic phân cấp hạng thành viên dựa trên số điểm hiện có
    IF NEW.rewardpoints >= 10000 THEN
        NEW.membershiplevel := 'Kim Cương';
    ELSIF NEW.rewardpoints >= 5000 THEN
        NEW.membershiplevel := 'Vàng';
    ELSIF NEW.rewardpoints >= 2000 THEN
        NEW.membershiplevel := 'Bạc';
    ELSE
        NEW.membershiplevel := 'Đồng'; 
    END IF;
    -- Biến NEW tự động ghi nhận giá trị mới trước khi lưu vào DB, không cần lệnh UPDATE
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Bắt sự kiện UPDATE xảy ra TRÊN CỘT rewardpoints của bảng Movie_Account
CREATE TRIGGER trg_Before_Update_Account_Rank
BEFORE UPDATE OF rewardpoints ON Movie_Account
FOR EACH ROW
EXECUTE FUNCTION fn_UpgradeMembership();


-- Trigger Chống bán vé lố giờ: Viết Trigger trên bảng Ticket để chặn thao tác bán vé (Throw Exception/Rollback) nếu thời điểm hiện tại (CURRENT_TIMESTAMP) đã vượt quá giờ chiếu (starttime + showdate của bảng Showtime).
CREATE OR REPLACE FUNCTION fn_PreventOverdueTicket()
RETURNS TRIGGER AS $$
DECLARE
    v_show_timestamp TIMESTAMP;
BEGIN
    -- Lấy thông tin ngày và giờ của suất chiếu, gộp lại thành 1 chuỗi thời gian cụ thể
    SELECT (s.showdate + s.starttime) INTO v_show_timestamp
    FROM MovieShow ms
    JOIN Showtime s ON ms.showtimeid = s.showtimeid
    WHERE ms.movieshowid = NEW.movieshowid;

    -- So sánh với thời gian thực tế của hệ thống máy chủ
    IF v_show_timestamp < CURRENT_TIMESTAMP THEN
        RAISE EXCEPTION 'Giao dịch từ chối: Không thể xuất vé, suất chiếu này đã bắt đầu hoặc đã kết thúc!';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_Before_Insert_Ticket_Overdue
BEFORE INSERT ON Ticket
FOR EACH ROW
EXECUTE FUNCTION fn_PreventOverdueTicket();

-- TẠO TRIGGER TÍNH TOÁN GIÁ VÉ TỰ ĐỘNG
CREATE OR REPLACE FUNCTION fn_calculate_ticket_price()
RETURNS TRIGGER AS $$
DECLARE
    v_seat_type VARCHAR(50);
    v_show_date DATE;
    v_day_of_week INT;
    v_base_price DECIMAL(15, 2) := 80000; -- Mức giá sàn cơ bản
    v_final_price DECIMAL(15, 2);
BEGIN
    -- Lấy thông tin loại ghế (seattype) của chiếc vé đang chuẩn bị xuất
    SELECT seattype INTO v_seat_type
    FROM Seat
    WHERE seatid = NEW.seatid;

    -- Lấy thông tin ngày chiếu (showdate) từ suất chiếu tương ứng
    SELECT s.showdate INTO v_show_date
    FROM MovieShow ms
    JOIN Showtime s ON ms.showtimeid = s.showtimeid
    WHERE ms.movieshowid = NEW.movieshowid;

    -- === BẮT ĐẦU LOGIC "TRA BẢNG GIÁ" ===
    v_final_price := v_base_price;

    -- 1. Áp dụng giá theo loại ghế
    IF v_seat_type = 'VIP' THEN
        v_final_price := v_final_price + 20000;
    ELSIF v_seat_type = 'Sweetbox' THEN
        v_final_price := v_final_price + 50000;
    END IF;

    -- 2. Áp dụng giá theo ngày (Cuối tuần)
    -- Hàm EXTRACT(ISODOW) trả về: 6 = Thứ 7, 7 = Chủ Nhật
    v_day_of_week := EXTRACT(ISODOW FROM v_show_date);
    IF v_day_of_week IN (6, 7) THEN
        v_final_price := v_final_price + 15000;
    END IF;

    -- === GHI ĐÈ KẾT QUẢ ===
    -- Bất chấp nhân viên thu ngân (hoặc code Python) cố tình truyền vào giá trị bao nhiêu,
    -- hệ thống sẽ tự động ép lại bằng giá trị đã tính toán chuẩn xác.
    NEW.ticketprice := v_final_price;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. GẮN TRIGGER VÀO BẢNG TICKET
CREATE TRIGGER trg_auto_calculate_ticket_price
BEFORE INSERT ON Ticket
FOR EACH ROW
EXECUTE FUNCTION fn_calculate_ticket_price();