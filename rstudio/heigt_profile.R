getwd()
setwd("C:/Users/W10/Documents/GitHub/height_profile/rstudio")
height_profile = read.csv("height_profile.csv")
height = height_profile$Height
decrease = 13+20*log10(height)

