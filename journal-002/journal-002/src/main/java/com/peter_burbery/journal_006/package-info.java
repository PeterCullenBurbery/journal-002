/**
* This package plans to make the find and sort windows independent. They will be able to moved around while you move the main window around, and they will have separate task bar entities. I will be using JFrame instead of JDialogue and FindWindow over FindDialog and SortingOptionsWindow over SortingOptionsDialog. The problem is that I have to get JournalTable.java to use these new ones and there's a lot I need to fix and stuff so I am going to hold off and wait because currently com.peter_burbery.journal_005 does not have window independence, but it works. The sort window was not working and that was because I had added it on one line in JournalTable, but there were other lines in JournalTable that referenced and referred to SortingOptionsDialog so I wasn't getting the functionality. The goal is to replace those references to SortingOptionsDialog with references to SortingOptionWindow. The problem is now once I replaced those, the sort window isn't working. I press change sort from descending to ascending and nothing happens. It does not reverse.
* 
* 
*
*/
package com.peter_burbery.journal_006;