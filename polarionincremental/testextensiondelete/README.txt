Description
===========

Control that only assignee user can delete WI
If Wi is unassigned then it can be removed or not dependent on value property ENABLE_DELETE_UNASSIGNEE

Property file located in root of hook jar 

How to use
==========
Hook should be used with WorkItemActionInterceptor extension. Just copy jar to hooks folder and enforce plugin reloading from managing wiki (or restart Polarion) 
