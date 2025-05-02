program ForLoopDemo;

procedure CountDownTimer(const timerStart: integer);
var
    i: INTEGER;
begin
    Writeln('Starting ForLoop countdown timer from ', timerStart);
    for i := timerStart downto 0 do
        begin
        Writeln('Inside For Loop Counting down: ', i);
        end;
    Writeln('Countdown timer hit 0');
end;

var
    n: INTEGER;
begin
    Write('Enter the number to start countdown timer: ');
    ReadLn(n);
    CountDownTimer(n);
end.
