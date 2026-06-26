create database G09_Cinema;
-- 1. NHÓM KHÁCH HÀNG & TÀI KHOẢN
CREATE TABLE Customer (
    customerid SERIAL PRIMARY KEY,
    fullname VARCHAR(255) NOT NULL,
    dob DATE
);
CREATE TABLE Movie_Account (
    account_id SERIAL PRIMARY KEY,
    customerid INT NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    registrationdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    membershiplevel VARCHAR(50),
    rewardpoints INT DEFAULT 0,
    CONSTRAINT fk_account_customer FOREIGN KEY (customerid) REFERENCES Customer(customerid)
);
-- 2. NHÓM NHÂN VIÊN & QUẢN LÝ
CREATE TABLE Manager (
    managerid SERIAL PRIMARY KEY,
    fullname VARCHAR(255) NOT NULL,
    dateofbirth DATE
);
CREATE TABLE Manager_Contact (
    contactid SERIAL PRIMARY KEY,
    managerid INT NOT NULL,
    phonenumber VARCHAR(15),
    email VARCHAR(100),
    CONSTRAINT fk_contact_manager FOREIGN KEY (managerid) REFERENCES Manager(managerid)
);
CREATE TABLE Staff (
    staffid SERIAL PRIMARY KEY,
    managerid INT,
    fullname VARCHAR(255) NOT NULL,
    role VARCHAR(100),
    dateofbirth DATE,
    address VARCHAR(255),
    salary DECIMAL(15, 2),
    CONSTRAINT fk_staff_manager FOREIGN KEY (managerid) REFERENCES Manager(managerid)
);
CREATE TABLE Staff_Contact (
    contactid SERIAL PRIMARY KEY,
    staffid INT NOT NULL,
    phonenumber VARCHAR(15),
    email VARCHAR(100),
    CONSTRAINT fk_contact_staff FOREIGN KEY (staffid) REFERENCES Staff(staffid)
);
-- 3. NHÓM PHIM, PHÒNG CHIẾU & SUẤT CHIẾU
CREATE TABLE Movie (
    movieid SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    director VARCHAR(255),
    genre VARCHAR(100),
    duration INT,
    language VARCHAR(50),
    agerating VARCHAR(10),
    releasedate DATE,
    synopsis VARCHAR(1000)
);
CREATE TABLE Cast_Member (
    movieid INT,
    "cast" VARCHAR(255),
    PRIMARY KEY (movieid, "cast"),
    CONSTRAINT fk_movie FOREIGN KEY (movieid) REFERENCES Movie(movieid) ON DELETE CASCADE
);
CREATE TABLE Showtime (
    showtimeid SERIAL PRIMARY KEY,
    starttime TIME NOT NULL,
    endtime TIME NOT NULL,
    showdate DATE NOT NULL
);
CREATE TABLE Room (
    roomid SERIAL PRIMARY KEY,
    roomname VARCHAR(50) NOT NULL,
    roomtype VARCHAR(50)
);
CREATE TABLE Seat (
    seatid SERIAL PRIMARY KEY,
    roomid INT NOT NULL,
    seatrow CHAR(2) NOT NULL,
    seatnumber INT NOT NULL,
    seattype VARCHAR(50),
    CONSTRAINT fk_seat_room FOREIGN KEY (roomid) REFERENCES Room(roomid)
);
CREATE TABLE MovieShow (
    movieshowid SERIAL PRIMARY KEY,
    movieid INT NOT NULL,
    roomid INT NOT NULL,
    showtimeid INT NOT NULL,
    status VARCHAR(50),
    CONSTRAINT fk_show_movie FOREIGN KEY (movieid) REFERENCES Movie(movieid),
    CONSTRAINT fk_show_room FOREIGN KEY (roomid) REFERENCES Room(roomid),
    CONSTRAINT fk_show_time FOREIGN KEY (showtimeid) REFERENCES Showtime(showtimeid)
);
-- 4. NHÓM HÓA ĐƠN & VÉ
CREATE TABLE Bill (
    billid SERIAL PRIMARY KEY,
    customerid INT,
    staffid INT,
    issuedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    discountamount DECIMAL(15, 2) DEFAULT 0,
    totalamount DECIMAL(15, 2) NOT NULL,
    CONSTRAINT fk_bill_customer FOREIGN KEY (customerid) REFERENCES Customer(customerid),
    CONSTRAINT fk_bill_staff FOREIGN KEY (staffid) REFERENCES Staff(staffid)
);
CREATE TABLE Payment (
    paymentid SERIAL PRIMARY KEY,
    billid INT NOT NULL,
    paymenttype VARCHAR(50),
    CONSTRAINT fk_payment_bill FOREIGN KEY (billid) REFERENCES Bill(billid)
);
CREATE TABLE Ticket (
    ticketid SERIAL PRIMARY KEY,
    movieshowid INT NOT NULL,
    seatid INT NOT NULL,
    billid INT NOT NULL,
    ticketprice DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50),
    bookingchannel VARCHAR(50),
    CONSTRAINT fk_ticket_show FOREIGN KEY (movieshowid) REFERENCES MovieShow(movieshowid),
    CONSTRAINT fk_ticket_seat FOREIGN KEY (seatid) REFERENCES Seat(seatid),
    CONSTRAINT fk_ticket_bill FOREIGN KEY (billid) REFERENCES Bill(billid)
);
-- 5. NHÓM SẢN PHẨM & CHI TIẾT BÁN HÀNG
CREATE TABLE Product (
    productid SERIAL PRIMARY KEY,
    productname VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    unitprice DECIMAL(15, 2) NOT NULL
);
CREATE TABLE Bill_Detail (
    billid INT NOT NULL,
    productid INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (billid, productid),
    CONSTRAINT fk_detail_bill FOREIGN KEY (billid) REFERENCES Bill(billid),
    CONSTRAINT fk_detail_product FOREIGN KEY (productid) REFERENCES Product(productid)
);
-- 6. NHÓM NHÀ CUNG CẤP & NHẬP HÀNG
CREATE TABLE Movie_Supplier (
    supplierid SERIAL PRIMARY KEY,
    suppliername VARCHAR(255) NOT NULL,
    contactname VARCHAR(255),
    address VARCHAR(255)
);
CREATE TABLE Supplier_Contact (
    contactid SERIAL PRIMARY KEY,
    supplierid INT NOT NULL,
    phonenumber VARCHAR(15),
    email VARCHAR(100),
    CONSTRAINT fk_contact_supplier FOREIGN KEY (supplierid) REFERENCES Movie_Supplier(supplierid)
);
CREATE TABLE ReceivingInvoice (
    receiveid SERIAL PRIMARY KEY,
    supplierid INT,
    managerid INT,
    movieid INT,
    receivedate DATE DEFAULT CURRENT_DATE,
    importprice DECIMAL(15, 2) NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_invoice_supplier FOREIGN KEY (supplierid) REFERENCES Movie_Supplier(supplierid),
    CONSTRAINT fk_invoice_manager FOREIGN KEY (managerid) REFERENCES Manager(managerid),
    CONSTRAINT fk_invoice_movie FOREIGN KEY (movieid) REFERENCES Movie(movieid)
);