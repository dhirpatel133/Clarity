PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS ClassroomAttemps (
     classroom_attempt_id INTEGER PRIMARY KEY AUTOINCREMENT,
     task_id INTEGER NOT NULL,
     user_id INTEGER NOT NULL,
     set_id INTEGER NOT NULL,
     card_id INTEGER NOT NULL,
     pronunciation INTEGER NOT NULL,
     accuracy INTEGER NOT NULL,
     fluency INTEGER NOT NULL,
     completeness INTEGER NOT NULL,
     attempt_date INTEGER NOT NULL,
     FOREIGN KEY(user_id) REFERENCES User(user_id),
     FOREIGN KEY(set_id) REFERENCES CardSet(set_id),
     FOREIGN KEY(card_id) REFERENCES Card(card_id),
     FOREIGN KEY(task_id) REFERENCES Tasks(task_id)
)