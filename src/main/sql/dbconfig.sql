create database biomed;
create user if not exists biomed identified by 'biomed';
grant all privileges on biomed.* to biomed;
alter user biomed identified by 'biomed';