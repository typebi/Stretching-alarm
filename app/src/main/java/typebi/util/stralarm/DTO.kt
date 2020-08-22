package typebi.util.stralarm

import android.database.Cursor

class DTO {
    var num : Int
    var name : String
    var startHour : Int
    var startMin : Int
    var endHour : Int
    var endMin : Int
    var interval : Int
    var settings : Int
    constructor(data : Cursor) {
        num = data.getInt(0)
        name = data.getString(1)
        startHour = data.getInt(2)
        startMin = data.getInt(3)
        endHour = data.getInt(4)
        endMin = data.getInt(5)
        interval = data.getInt(6)
        settings = data.getInt(7)
    }
    constructor(num:Int, name:String, startHour:Int, startMin:Int, endHour:Int, endMin:Int, interval:Int, settings:Int){
        this.num = num
        this.name = name
        this.startHour = startHour
        this.startMin = startMin
        this.endHour = endHour
        this.endMin = endMin
        this.interval = interval
        this.settings = settings
    }
}