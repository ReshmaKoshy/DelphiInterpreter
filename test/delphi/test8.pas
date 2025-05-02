program WhileLoopDemo;

procedure CountDownTimer(const timerStart: integer);
var
    i: INTEGER;
begin
    Writeln('Starting While Loop countdown timer from ', timerStart);
    i := timerStart;
    while i >= 0 do
    begin
        Writeln('Inside While Loop Counting down: ', i);
        i := i - 1;
    end;
    Writeln('Countdown timer hit ', i);
end;

var
    n: INTEGER;
begin
    Write('Enter the number to start countdown timer: ');
    ReadLn(n);
    CountDownTimer(n);
end.
