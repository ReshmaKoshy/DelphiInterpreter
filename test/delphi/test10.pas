program BreakDemo;

procedure CountDownTimer(const timerStart: integer);
var
    i: INTEGER;
begin
    Writeln('Statement Before While Loop: Starting While Loop countdown timer from ', timerStart);
    i := timerStart;
    while i >= 0 do
    begin
        Writeln('Statement Inside While Loop: Before Break: ', i);
        i := i - 1;
        break;
        Writeln('Statement Inside While Loop: After Break: ', i);
    end;
    Writeln('Statement After While Loop: Countdown timer hit ', i);
end;

var
    n: INTEGER;
begin
    Write('Enter the number to start countdown timer: ');
    ReadLn(n);
    CountDownTimer(n);
end.
