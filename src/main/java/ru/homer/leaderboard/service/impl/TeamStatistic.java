package ru.homer.leaderboard.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.homer.leaderboard.entity.IssueDto;
import ru.homer.leaderboard.enums.IssueType;
import ru.homer.leaderboard.enums.IssueTypeByBug;
import ru.homer.leaderboard.service.Statistic;
import ru.homer.leaderboard.service.TimeSheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vadmurzakov on 22.07.17.
 */
@Service
public class TeamStatistic implements Statistic {

    private final TimeSheet jiraTimeSheet;
    private List<String> teamHomer = new ArrayList<>(Arrays.asList("eplotnikov", "vmurzakov", "mnikolaenko", "ytrunov", "rnemykin", "vuvarov", "akovlyashenko", "ilysenko"));

    @Autowired
    public TeamStatistic(TimeSheet jiraTimeSheet) {
        this.jiraTimeSheet = jiraTimeSheet;
    }

    @Override
    public List<ru.homer.leaderboard.entity.Statistic> getStatisticsOnTeamFor6Month() {
        List<ru.homer.leaderboard.entity.Statistic> statistics = new ArrayList<>();
        for (String username : teamHomer) {
            ru.homer.leaderboard.entity.Statistic statistic = new ru.homer.leaderboard.entity.Statistic();
            List<IssueDto> issues = jiraTimeSheet.getAllIssuesForLastMonthByUser(username, 6);

            statistic.setUsername(username);
            statistic.setCountIssues(issues.size());

            statistic.setCountProductBugs(calcCountBugFilterByType(IssueType.PRODUCT_BUG, issues));
            statistic.setCountSimpleBugs(calcCountBugFilterByType(IssueType.SIMPLE_BUG, issues));
            statistic.setCountAllBugs(statistic.getCountProductBugs() + statistic.getCountSimpleBugs());

            statistic.setTimeOnProductBugs(calcTimeFilterByType(IssueType.PRODUCT_BUG, issues));
            statistic.setTimeOnSimpleBugs(calcTimeFilterByType(IssueType.SIMPLE_BUG, issues));
            statistic.setTimeOnAllBugs(statistic.getTimeOnProductBugs() + statistic.getTimeOnSimpleBugs());

            statistic.setHourPerProductBug((double) (statistic.getTimeOnProductBugs() / 60 / statistic.getCountProductBugs()));
            statistic.setHourPerSimpleBug((double) (statistic.getTimeOnSimpleBugs() / 60 / statistic.getCountSimpleBugs()));

            //todo тут надо посчитать все время за все задачи, пока тут только баги
            statistic.setAllTimeOnIssues(statistic.getTimeOnAllBugs());

            statistics.add(statistic);
        }
        return statistics;
    }

    @Override
    public Long calcCountBugFilterByType(IssueType issueType, List<IssueDto> issueDtos) {
        Long count = 0L;
        List<Long> idsIssueType = IssueTypeByBug.getIdsByType(issueType);
        for (IssueDto issueDto : issueDtos) {
            if (idsIssueType.contains(issueDto.getIssueType().getId())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Long calcTimeFilterByType(IssueType issueType, List<IssueDto> issueDtos) {
        Long time = 0L;
        List<Long> idsIssueType = IssueTypeByBug.getIdsByType(issueType);
        for (IssueDto issueDto : issueDtos) {
            if (idsIssueType.contains(issueDto.getIssueType().getId())) {
                time += issueDto.getWorkTime();
            }
        }
        return time;
    }

}