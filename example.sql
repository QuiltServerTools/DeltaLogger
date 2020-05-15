SELECT CT.date, R.name, CT.itemcount, P.name
FROM container_transactions as CT
INNER JOIN registry as R ON CT.itemtype = R.id
INNER JOIN players as P ON CT.playerid = P.id
WHERE P.name = ""
ORDER BY CT.date DESC LIMIT 50;

SELECT PC.date, R.name, P.name
FROM placements as PC
INNER JOIN registry as R ON PC.type = R.id
INNER JOIN players as P ON PC.playerid = P.id
WHERE P.name = "" AND PC.placed = 1
ORDER BY PC.date DESC LIMIT 400;

SELECT C.uuid, CT.date, R.name, CT.itemcount, P.name
FROM container_transactions as CT
INNER JOIN registry as R ON CT.itemtype = R.id
INNER JOIN players as P ON CT.playerid = P.id
INNER JOIN containers as C ON CT.containerid = C.id
WHERE C.x=0 AND C.y=93 AND C.z=25
ORDER BY CT.date DESC LIMIT 200