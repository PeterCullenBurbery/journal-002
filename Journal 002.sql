CREATE TABLE JOURNAL_002 (
    JOURNAL_002            VARCHAR2(4000),
    -- Additional columns for note and dates
    date_created           TIMESTAMP(9) WITH TIME ZONE DEFAULT systimestamp(9) NOT NULL,
    date_updated           TIMESTAMP(9) WITH TIME ZONE,
    date_created_or_updated TIMESTAMP(9) WITH TIME ZONE GENERATED ALWAYS AS ( coalesce(date_updated, date_created) ) VIRTUAL,
    JOURNAL_002_id         RAW(16) DEFAULT sys_guid() PRIMARY KEY
);

-- Trigger to update date_updated for JOURNAL_002
CREATE OR REPLACE TRIGGER set_date_updated_JOURNAL_002
    BEFORE UPDATE ON JOURNAL_002
    FOR EACH ROW
BEGIN
    :new.date_updated := systimestamp;
END;
/
