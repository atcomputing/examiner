package nl.atcomputing.examtrainer;


import java.util.List;

import android.os.Message;

public interface FeedParser {
    List<Message> parse();
}