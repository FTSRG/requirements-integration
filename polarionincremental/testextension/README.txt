Description
===========

Hook checking length for title field. If it over then defined number of symbols then Polarion shows warning message without saving changes.
Size of title can be defined in property file. If value equal -1 then length is unlimited.   
 
How to use
==========
Hook should be used with WorkItemActionInterceptor extension. Just copy jar to hooks folder and enforce plugin reloading from managing wiki (or restart Polarion)    
 