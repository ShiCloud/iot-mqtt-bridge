DROP TABLE IF EXISTS t_device_info;
CREATE TABLE t_device_info (
  id int AUTO_INCREMENT,
  device_id smallint NOT NULL,
  msg_code int,
	msg_value int,
  time_stamp datetime,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;