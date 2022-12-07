
  CREATE TABLE users
  (
    user_id INT NOT NULL AUTO_INCREMENT,
    company_name VARCHAR(256) NOT NULL,
    user_name VARCHAR(256) NOT NULL,
    PRIMARY KEY(user_id)
  );

  CREATE TABLE schedules
  (
      user_id INT NOT NULL,
      working_date DATE NOT NULL,
      number_of_floors VARCHAR(256) NOT NULL,
      place VARCHAR(256) NOT NULL,
      work_content VARCHAR(256) NOT NULL,
      number_of_people VARCHAR(256) NOT NULL,
      PRIMARY KEY(user_id),
      CONSTRAINT fk_schedules_users
          FOREIGN KEY (user_id) REFERENCES users (user_id)  ON DELETE CASCADE
  );