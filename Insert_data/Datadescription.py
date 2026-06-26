from faker import Faker
import random
import os  
fake = Faker('vi_VN')

# --- THIẾT LẬP SỐ LƯỢNG CHÍNH XÁC ---
NUM_CUSTOMERS = 500
NUM_MANAGERS = 30
NUM_STAFF = 150
NUM_MOVIES = 500
NUM_SHOWTIMES = 50
NUM_ROOMS = 20
NUM_SHOWS = 500
NUM_BILLS = 500
NUM_PRODUCTS = 50
NUM_SUPPLIERS = 30
NUM_RECEIPTS = 100

# --- BỘ TỪ ĐIỂN TÊN 3 CHỮ ---
ho_vn = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương', 'Lý']
dem_vn = ['Văn', 'Thị', 'Ngọc', 'Hoàng', 'Minh', 'Hữu', 'Đức', 'Xuân', 'Thu', 'Thanh', 'Bảo', 'Gia', 'Hải', 'Thái', 'Đình', 'Thúy', 'Mai', 'Anh', 'Tuấn']
ten_vn = ['Anh', 'Tuấn', 'Dũng', 'Linh', 'Trang', 'Hoa', 'Hải', 'Sơn', 'Cường', 'Phong', 'Hà', 'Lan', 'Ngọc', 'Huy', 'Thảo', 'Nhung', 'Nam', 'Việt', 'Khánh', 'Đạt']

def gen_name():
    return f"{random.choice(ho_vn)} {random.choice(dem_vn)} {random.choice(ten_vn)}"

def clean_txt(text):
    return str(text).replace("'", "''")
script_dir = os.path.dirname(os.path.abspath(__file__))

# Nối tên file SQL vào thư mục gốc đó
file_path = os.path.join(script_dir, "DULIEU_RAPPHIM.sql")

# Sửa lại lệnh open() dùng biến file_path vừa tạo
with open(file_path, "w", encoding="utf-8") as f:
    f.write("-- === 0. RESET DATABASE MỚI HOÀN TOÀN === --\n")
    f.write("TRUNCATE TABLE Movie_Account, Customer, Manager, Manager_Contact, Staff, Staff_Contact, Movie, Cast_Member, Showtime, Room, Seat, MovieShow, Product, Bill, Payment, Bill_Detail, Ticket, Movie_Supplier, Supplier_Contact, ReceivingInvoice RESTART IDENTITY CASCADE;\n\n")
    # ==========================================
    # 1. NHÓM KHÁCH HÀNG & TÀI KHOẢN
    # ==========================================
    f.write(f"-- === 1. BẢNG CUSTOMER ({NUM_CUSTOMERS} DÒNG) === --\n")
    for _ in range(NUM_CUSTOMERS):
        dob = fake.date_of_birth(minimum_age=16, maximum_age=60).strftime('%Y-%m-%d')
        f.write(f"INSERT INTO Customer (fullname, dob) VALUES ('{gen_name()}', '{dob}');\n")

    f.write("\n-- === BẢNG MOVIE_ACCOUNT === --\n")
    for i in range(1, NUM_CUSTOMERS + 1):
        uname = clean_txt(f"user_{i}_{fake.user_name()}")
        level = random.choice(['Đồng', 'Bạc', 'Vàng', 'Kim Cương'])
        pts = random.randint(0, 5000)
        f.write(f"INSERT INTO Movie_Account (customerid, username, password_hash, membershiplevel, rewardpoints) VALUES ({i}, '{uname}', '{fake.sha256()}', '{level}', {pts});\n")

    # ==========================================
    # 2. NHÓM NHÂN VIÊN & QUẢN LÝ
    # ==========================================
    f.write(f"\n-- === 2. BẢNG MANAGER ({NUM_MANAGERS} DÒNG) === --\n")
    for _ in range(NUM_MANAGERS):
        dob = fake.date_of_birth(minimum_age=28, maximum_age=55).strftime('%Y-%m-%d')
        f.write(f"INSERT INTO Manager (fullname, dateofbirth) VALUES ('{gen_name()}', '{dob}');\n")

    f.write("\n-- === BẢNG MANAGER_CONTACT === --\n")
    for i in range(1, NUM_MANAGERS + 1):
        f.write(f"INSERT INTO Manager_Contact (managerid, phonenumber, email) VALUES ({i}, '{fake.phone_number()[:15]}', '{fake.email()}');\n")

    f.write(f"\n-- === BẢNG STAFF ({NUM_STAFF} DÒNG) === --\n")
    roles = ['Bán vé', 'Soát vé', 'Phục vụ', 'Kỹ thuật máy chiếu', 'Bảo vệ']
    for _ in range(NUM_STAFF):
        m_id = random.randint(1, NUM_MANAGERS)
        dob = fake.date_of_birth(minimum_age=18, maximum_age=40).strftime('%Y-%m-%d')
        salary = random.randint(5, 15) * 1000000
        f.write(f"INSERT INTO Staff (managerid, fullname, role, dateofbirth, address, salary) VALUES ({m_id}, '{gen_name()}', '{random.choice(roles)}', '{dob}', '{clean_txt(fake.address())}', {salary});\n")

    f.write("\n-- === BẢNG STAFF_CONTACT === --\n")
    for i in range(1, NUM_STAFF + 1):
        f.write(f"INSERT INTO Staff_Contact (staffid, phonenumber, email) VALUES ({i}, '{fake.phone_number()[:15]}', '{fake.email()}');\n")

    # ==========================================
    # 3. NHÓM PHIM & PHÒNG CHIẾU
    # ==========================================
    f.write(f"\n-- === 3. BẢNG MOVIE ({NUM_MOVIES} DÒNG) === --\n")
    for _ in range(NUM_MOVIES):
        title = clean_txt(fake.catch_phrase())
        genre = random.choice(['Hành động', 'Kinh dị', 'Hài hước', 'Tình cảm', 'Viễn tưởng'])
        lang = random.choice(['Tiếng Việt', 'Tiếng Anh', 'Tiếng Hàn'])
        rating = random.choice(['P', 'C13', 'C16', 'C18'])
        release = fake.date_between(start_date='-2y', end_date='+1m').strftime('%Y-%m-%d')
        synopsis = clean_txt(fake.text(max_nb_chars=150))
        # Nếu bảng Movie của bạn vẫn còn cột castmembers thì giữ lại phần gen_name cho cột đó
        # Ở đây tạm bỏ cột castmembers ra khỏi INSERT bảng Movie vì đã có bảng Cast_Member riêng bên dưới
        f.write(f"INSERT INTO Movie (title, director, genre, duration, language, agerating, releasedate, synopsis) VALUES ('{title}', '{gen_name()}', '{genre}', {random.randint(90, 180)}, '{lang}', '{rating}', '{release}', '{synopsis}');\n")

    f.write("\n-- === BẢNG CAST_MEMBER === --\n")
    for i in range(1, NUM_MOVIES + 1):
        actors = set([gen_name() for _ in range(random.randint(2, 4))])
        for actor in actors:
            f.write(f"INSERT INTO Cast_Member (movieid, \"cast\") VALUES ({i}, '{actor}');\n")
    f.write(f"\n-- === BẢNG SHOWTIME ({NUM_SHOWTIMES} DÒNG) === --\n")
    for _ in range(NUM_SHOWTIMES):
        # Logic Giờ chiếu thực tế: Bắt đầu từ 8h sáng đến 21h tối, phim dài ~ 2 tiếng
        start_hour = random.randint(8, 21)
        mins = random.choice(['00', '15', '30', '45'])
        st = f"{start_hour:02d}:{mins}:00"
        et = f"{start_hour + 2:02d}:{mins}:00" 
        
        # FIX TRIGGER: Ép ngày chiếu phải nằm ở TƯƠNG LAI (từ NGÀY MAI trở đi)
        sd = fake.date_between(start_date='+1d', end_date='+30d').strftime('%Y-%m-%d')
        
        f.write(f"INSERT INTO Showtime (starttime, endtime, showdate) VALUES ('{st}', '{et}', '{sd}');\n")

    f.write(f"\n-- === BẢNG ROOM & SEAT ({NUM_ROOMS} PHÒNG) === --\n")
    for i in range(1, NUM_ROOMS + 1):
        f.write(f"INSERT INTO Room (roomname, roomtype) VALUES ('Phòng {i:02d}', '{random.choice(['Standard', 'IMAX', '3D'])}');\n")

    for r in range(1, NUM_ROOMS + 1):
        for row_char in ['A', 'B', 'C', 'D', 'E', 'F']:
            for num in range(1, 11):
                f.write(f"INSERT INTO Seat (roomid, seatrow, seatnumber, seattype) VALUES ({r}, '{row_char}', {num}, '{random.choice(['Thường', 'VIP', 'Sweetbox'])}');\n")

    f.write(f"\n-- === BẢNG MOVIESHOW ({NUM_SHOWS} DÒNG) === --\n")
    for _ in range(NUM_SHOWS):
        m_id, r_id, s_id = random.randint(1, NUM_MOVIES), random.randint(1, NUM_ROOMS), random.randint(1, NUM_SHOWTIMES)
        
        # FIX TRIGGER: Đã là suất chiếu ở tương lai (+1d) thì status logic nhất phải là 'Sắp chiếu'
        f.write(f"INSERT INTO MovieShow (movieid, roomid, showtimeid, status) VALUES ({m_id}, {r_id}, {s_id}, 'Sắp chiếu');\n")
    # ==========================================
    # 4. NHÓM HÓA ĐƠN & BÁN HÀNG
    # ==========================================
    f.write(f"\n-- === 4. BẢNG PRODUCT ({NUM_PRODUCTS} DÒNG) === --\n")
    for _ in range(NUM_PRODUCTS):
        pname = clean_txt(fake.word().capitalize() + " " + random.choice(['Size M', 'Size L', 'Khổng lồ']))
        price = random.randint(20, 150) * 1000
        f.write(f"INSERT INTO Product (productname, category, unitprice) VALUES ('{pname}', '{random.choice(['Nước', 'Bắp', 'Combo'])}', {price});\n")

    f.write(f"\n-- === BẢNG BILL, PAYMENT, BILL_DETAIL, TICKET ({NUM_BILLS} DÒNG) === --\n")
    for i in range(1, NUM_BILLS + 1):
        c_id, s_id = random.randint(1, NUM_CUSTOMERS), random.randint(1, NUM_STAFF)
        total = random.randint(100, 1000) * 1000
        f.write(f"INSERT INTO Bill (customerid, staffid, discountamount, totalamount) VALUES ({c_id}, {s_id}, {random.choice([0, 10000, 20000])}, {total});\n")
        f.write(f"INSERT INTO Payment (billid, paymenttype) VALUES ({i}, '{random.choice(['Tiền mặt', 'Thẻ', 'MoMo'])}');\n")
        
        for p_id in random.sample(range(1, NUM_PRODUCTS + 1), random.randint(1, 2)):
            f.write(f"INSERT INTO Bill_Detail (billid, productid, quantity) VALUES ({i}, {p_id}, {random.randint(1, 3)});\n")

        ms_id, seat_id = random.randint(1, NUM_SHOWS), random.randint(1, NUM_ROOMS * 60)
        f.write(f"INSERT INTO Ticket (movieshowid, seatid, billid, ticketprice, status, bookingchannel) VALUES ({ms_id}, {seat_id}, {i}, {random.choice([60000, 80000])}, 'Đã thanh toán', '{random.choice(['Quầy', 'App'])}');\n")

    # ==========================================
    # 5. NHÀ CUNG CẤP & NHẬP HÀNG
    # ==========================================
    f.write(f"\n-- === 5. BẢNG SUPPLIER & RECEIVING ({NUM_SUPPLIERS} SUPPLIER, {NUM_RECEIPTS} RECEIPT) === --\n")
    for _ in range(NUM_SUPPLIERS):
        f.write(f"INSERT INTO Movie_Supplier (suppliername, contactname, address) VALUES ('{clean_txt(fake.company())}', '{gen_name()}', '{clean_txt(fake.address())}');\n")

    for i in range(1, NUM_SUPPLIERS + 1):
        f.write(f"INSERT INTO Supplier_Contact (supplierid, phonenumber, email) VALUES ({i}, '{fake.phone_number()[:15]}', '{fake.email()}');\n")

    for _ in range(NUM_RECEIPTS):
        sup_id, mng_id, mov_id = random.randint(1, NUM_SUPPLIERS), random.randint(1, NUM_MANAGERS), random.randint(1, NUM_MOVIES)
        f.write(f"INSERT INTO ReceivingInvoice (supplierid, managerid, movieid, importprice, quantity) VALUES ({sup_id}, {mng_id}, {mov_id}, {random.randint(10, 500)*1000000}, {random.randint(1, 5)});\n")

print("File DULIEU_RAPPHIM.sql đã được tạo thành công!")