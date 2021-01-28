package com.gitee.kooder.indexer;

import com.gitee.kooder.utils.BatchTaskRunner;

import java.util.ArrayList;
import java.util.List;

public class CodeDocumentAction  {

    public static void main(String[] args){
        List<Integer> tasks = new ArrayList<>();
        for(int i=0;i<100;i++)
            tasks.add(i);
        BatchTaskRunner.execute(tasks, 13, list -> System.out.printf("%s -> %s\n", Thread.currentThread().getName(), list));
    }
}
