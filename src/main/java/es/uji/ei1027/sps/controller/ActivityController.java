package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.ActivityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/activity")
public class ActivityController {

    private ActivityDao activityDao;

    @Autowired
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @RequestMapping("/list")
    public String listActivities(Model model) {
        model.addAttribute("activities", activityDao.getActivities());
        return "activity/list";
    }
}
