# 🎬 CinemaCore: Hệ Thống Cơ Sở Dữ Liệu Quản Lý Vận Hành Rạp Chiếu Phim

**CinemaCore** là dự án thiết kế và xây dựng kiến trúc Cơ sở dữ liệu (CSDL) quan hệ toàn diện, phục vụ cho quá trình vận hành, kinh doanh và điều phối tài nguyên tại một cụm rạp chiếu phim hiện đại. Dự án không chỉ dừng lại ở việc lưu trữ dữ liệu tĩnh, mà còn đóng vai trò như một "động cơ" (engine) xử lý các quy tắc kinh doanh (Business Rules), kiểm soát giao dịch thời gian thực và tối ưu hóa hiệu suất báo cáo.

---

## 🌟 I. Bối cảnh & Bài toán nghiệp vụ (Problem Statement)

Ngành công nghiệp điện ảnh và vận hành rạp chiếu phim đặc thù bởi khối lượng giao dịch khổng lồ diễn ra trong các khung giờ cao điểm (ví dụ: các dịp lễ, ra mắt phim bom tấn). Một hệ thống quản lý rạp phim phải đối mặt với những bài toán hóc búa sau:

1. **Xung đột tài nguyên thời gian thực (Race Condition):** Hàng ngàn khách hàng có thể cùng truy cập ứng dụng để đặt vé vào cùng một thời điểm. Hệ thống cần cơ chế giữ chỗ và khóa tài nguyên tuyệt đối để ngăn chặn tình trạng "hai người cùng thanh toán cho một chiếc ghế".
2. **Logic tính giá phức tạp & Dễ sai sót:** Giá vé không cố định mà biến thiên liên tục dựa trên nhiều yếu tố (loại ghế VIP/Thường, ngày thường/cuối tuần, độ tuổi khách hàng). Việc để nhân viên tự tính toán hoặc nhập tay tiềm ẩn rủi ro thất thoát doanh thu rất lớn.
3. **Mô hình "Giỏ hàng" hợp nhất:** Khách hàng hiện đại không chỉ mua vé, họ mua các gói Combo (Vé + Bắp + Nước) và thanh toán bằng nhiều hình thức (Voucher, Thẻ tín dụng, Tiền mặt) trên cùng một hóa đơn.
4. **Nút thắt cổ chai trong Báo cáo (Reporting Bottlenecks):** Việc truy xuất doanh thu tổng hợp từ hàng triệu giao dịch lẻ tẻ thường gây quá tải hệ thống, làm chậm trễ các quyết định của ban quản lý.

---

## 🎯 II. Mục tiêu Dự án (Project Objectives)

Dự án **CinemaCore** được phát triển nhằm giải quyết triệt để các bài toán trên thông qua nền tảng CSDL mạnh mẽ, với các mục tiêu cốt lõi:

* **Tự động hóa hoàn toàn ở tầng Data:** Chuyển dịch tối đa các quy tắc nghiệp vụ (Kiểm tra tuổi, tính giá vé, phân hạng thành viên) xuống tầng CSDL thông qua Triggers và Functions để đảm bảo tính toàn vẹn, thay vì phụ thuộc hoàn toàn vào Backend.
* **Đảm bảo tính ACID tuyệt đối:** Thiết kế kiến trúc kiểm soát đồng thời (Concurrency Control) chặt chẽ cho quá trình giao dịch thanh toán và chọn ghế.
* **Kiến trúc dữ liệu Module hóa:** Phân tách rõ ràng các nhóm nghiệp vụ (Nhân sự, Kho hàng, Khách hàng, Suất chiếu) để dễ dàng bảo trì và mở rộng trong tương lai.
* **Tối ưu hóa Hiệu năng (High Performance):** Đảm bảo hệ thống vẫn phản hồi ở tốc độ mili-giây kể cả khi dữ liệu tăng trưởng lên mức hàng triệu bản ghi thông qua các chiến lược đánh chỉ mục (Indexing) và thực thể hóa dữ liệu (Materialization).

---

## 🏢 III. Bức tranh Tổng quan Phân hệ (Core Modules)

Hệ thống được chia thành 5 trụ cột nghiệp vụ chính, hoạt động tương hỗ lẫn nhau tạo thành một vòng tuần hoàn khép kín:

### 1. Quản trị Khách hàng & Chương trình Khách hàng thân thiết (Loyalty)
Quản lý vòng đời khách hàng từ lúc đăng ký đến khi phát sinh giao dịch. Hệ thống tự động theo dõi lịch sử chi tiêu, tích lũy điểm thưởng và điều chỉnh hạng thành viên (Member, VIP) mà không cần thao tác thủ công, làm nền tảng cho các chiến dịch Marketing sau này.

### 2. Quản lý Tài nguyên Không gian & Thời gian (Schedules & Operations)
Đây là trái tim của rạp phim. Phân hệ này điều phối lịch chiếu phim vào các phòng chiếu theo các khung giờ cụ thể. Hệ thống thiết lập sẵn các ràng buộc chặt chẽ để tự động báo lỗi và ngăn chặn mọi thao tác xếp trùng lịch chiếu, tối ưu hóa công suất sử dụng phòng.

### 3. Vận hành Giao dịch Thương mại (Unified Billing & Ticketing)
Mô hình hóa toàn bộ luồng tiền của rạp. Một giao dịch (Hóa đơn) có khả năng đóng gói cả dịch vụ (Vé xem phim) và hàng hóa vật lý (Đồ ăn, thức uống). Phân hệ hỗ trợ chia nhỏ nguồn thanh toán linh hoạt và theo dõi dư nợ giao dịch đến khi hoàn tất.

### 4. Luồng Phê duyệt Cung ứng (Supply Chain)
Quản lý các đối tác phân phối bản quyền phim. Mọi bộ phim mới muốn được đưa vào rạp để xếp lịch đều phải trải qua luồng khởi tạo và phê duyệt chứng từ/hóa đơn nhập khẩu rõ ràng.

### 5. Quản trị Nhân sự & Phân cấp (HR & Hierarchies)
Xác lập định danh và ánh xạ nhân sự vào sơ đồ tổ chức (Nhân viên - Quản lý). Đảm bảo tính minh bạch trong việc phân quyền xử lý giao dịch và truy vết luồng báo cáo.

---

## 🛠️ IV. Các Kỹ thuật Tối ưu Nổi bật (Technical Highlights)

Dự án áp dụng sâu rộng các tính năng nâng cao của Hệ quản trị CSDL quan hệ:

* **Automated Data Validation (Triggers):** Triển khai các "người gác cổng" tự động kiểm tra đối chiếu độ tuổi khách hàng với nhãn phim giới hạn (T13, T16, T18) và tự động áp dụng biểu phí động (Dynamic Pricing) trước khi dữ liệu kịp ghi vào hệ thống.
* **Report Materialization (Khung nhìn thực thể hóa):** Ứng dụng Materialized Views để lưu trữ trước các báo cáo doanh thu phức tạp, giải phóng sức mạnh tính toán cho máy chủ và đưa tốc độ xuất báo cáo về mức tức thời.
* **Smart Indexing Strategy:** Phối hợp đa dạng các thuật toán tìm kiếm như **B-Tree** (cho tra cứu dữ liệu khoảng), **Hash Index** (cho tra cứu định danh chính xác tuyệt đối) và **GIN Index** (xây dựng bộ máy tìm kiếm toàn văn bản - Full text search cho thư viện phim).
