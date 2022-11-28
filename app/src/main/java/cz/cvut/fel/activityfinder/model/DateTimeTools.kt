package cz.cvut.fel.activityfinder.model

import java.time.format.DateTimeFormatter

class DateTimeTools {

    companion object {
        val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d. M. y HH:mm")
    }

}