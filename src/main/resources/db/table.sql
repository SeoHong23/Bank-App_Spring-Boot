
create table user_tb(
                        id int auto_increment primary key,
                        username varchar(50) not null unique,
                        password varchar(100) not null,
                        fullname varchar(50) not null,
                        created_at timestamp not null default now()
);


-- 사용자 계좌 테이블 설계
create table account_tb(
                           id int auto_increment primary key,
                           number varchar(30) not null unique,
                           password varchar(20) not null,
                           balance bigint not null comment '계좌잔액',
                           user_id int,
                           created_at timestamp not null default now()
);

-- 계좌 사용 내역 테이블 - 입금, 출금, 이체
create table history_tb(
                           id int auto_increment primary key comment '거래내역 ID',
                           amount bigint not null comment '거래 금액',
                           w_account_id int comment '출금계좌ID',
                           d_account_id int comment '입금계좌ID',
                           w_balance bigint comment '출금 요청된 후 계좌 잔액',
                           d_balance bigint comment '입금 요청된 후 계좌 잔액',
                           created_at timestamp not null default now()
);

alter table user_tb
    add column origin_file_name varchar(200);

alter table user_tb
    add column upload_file_name varchar(200);