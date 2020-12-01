package com.gitee.search.query;

/**
 * 排序工具包，具体使用请参考 QueryHelper
 * @author Winter Lau<javayou@gmail.com>
 * @see com.gitee.search.query.QueryHelper
 */
public class ScoreHelper {

    /**
     * 自定义仓库的搜索评分规则
     * @param score
     * @param recomm
     * @param stars
     * @param gindex
     * @return
     */
    public static double repoSort(double score, double recomm, double stars, double gindex) {
        //System.out.printf("score:%.2f, recomm: %f, stars: %f, gindex: %f", score, recomm, stars, gindex);
        if(score >= 40) {
            score += (recomm * 100);
            if(stars >= 100)
                score += (stars/100) * 10;
            if(gindex > 80)
                score += 100;
            else if(gindex > 50)
                score += 20;
        }
        //System.out.println(" new score: " + score);
        return score;
    }

    /**
     * 自定义 Issue 评分
     * @param score
     * @return
     */
    public static double issueSort(double score) {
        return 0.1d;
    }

}
